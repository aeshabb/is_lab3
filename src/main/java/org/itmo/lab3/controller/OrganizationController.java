package org.itmo.lab3.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.itmo.lab3.model.Coordinates;
import org.itmo.lab3.model.Organization;
import org.itmo.lab3.service.OrganizationService;
import org.itmo.lab3.util.Page;
import org.itmo.lab3.util.PageRequest;
import org.itmo.lab3.util.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, "coordinates.x", new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Float.class, "coordinates.y", new CustomNumberEditor(Float.class, true));
    }

    @GetMapping(value = {"", "/"})
    @Transactional(readOnly = true)
    public String index(@RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(name = "sort", required = false) String sort,
                        @RequestParam(name = "order", required = false) String order,
                        @RequestParam(name = "filter", required = false) String filter,
                        Model model) {
        PageRequest pageRequest;
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sorting = direction == Sort.Direction.DESC ? 
                Sort.by(Sort.Order.desc(sort)) : Sort.by(Sort.Order.asc(sort));
            pageRequest = PageRequest.of(page, size, sorting);
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        Page<Organization> organizationsPage;
        if (filter != null && !filter.isEmpty()) {
            List<Organization> filtered = organizationService.findOrganizationsByName(filter);
            List<Organization> sorted = sortOrganizations(filtered, sort, order);
            int start = page * size;
            int end = Math.min(start + size, sorted.size());
            List<Organization> pageContent = start < sorted.size() ? sorted.subList(start, end) : java.util.Collections.emptyList();
            organizationsPage = new org.itmo.lab3.util.PageImpl<>(pageContent, pageRequest, sorted.size());
        } else {
            organizationsPage = organizationService.getAllOrganizations(pageRequest);
        }

        model.addAttribute("organizationsList", organizationsPage.getContent());
        model.addAttribute("organizations", organizationsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("filter", filter);
        return "index";
    }

    private List<Organization> sortOrganizations(List<Organization> source, String sort, String order) {
        if (sort == null || sort.isEmpty()) {
            return source;
        }
        java.util.Comparator<Organization> comparator;
        switch (sort) {
            case "name" -> comparator = java.util.Comparator.comparing(Organization::getName, java.util.Comparator.nullsLast(String::compareToIgnoreCase));
            case "type" -> comparator = java.util.Comparator.comparing(o -> o.getType() != null ? o.getType().name() : null, java.util.Comparator.nullsLast(String::compareToIgnoreCase));
            case "creationDate" -> comparator = java.util.Comparator.comparing(Organization::getCreationDate, java.util.Comparator.nullsLast(java.time.LocalDate::compareTo));
            case "rating" -> comparator = java.util.Comparator.comparing(Organization::getRating, java.util.Comparator.nullsLast(Double::compareTo));
            case "annualTurnover" -> comparator = java.util.Comparator.comparing(Organization::getAnnualTurnover, java.util.Comparator.nullsLast(Long::compareTo));
            case "employeesCount" -> comparator = java.util.Comparator.comparing(Organization::getEmployeesCount, java.util.Comparator.nullsLast(Integer::compareTo));
            default -> comparator = null;
        }
        if (comparator == null) {
            return source;
        }
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        return source.stream().sorted(comparator)
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/view/{id}")
    @Transactional(readOnly = true)
    public String viewOrganization(@PathVariable Long id, Model model) {
        System.out.println("DEBUG: viewOrganization called with id=" + id);
        return organizationService.getOrganizationById(id)
                .map(org -> {
                    System.out.println("DEBUG: Found organization: " + org.getId() + ", " + org.getName());
                    model.addAttribute("organization", org);
                    return "view";
                })
                .orElseGet(() -> {
                    System.out.println("DEBUG: Organization not found with id=" + id);
                    return "redirect:/";
                });
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("organization", new Organization());
        model.addAttribute("existingAddresses", organizationService.getAllAddresses());
        model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
        return "form";
    }

    @PostMapping("/create")
    public String createOrganization(@ModelAttribute Organization organization,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        normalizeCoordinates(organization, result, request);

        if (organization.getOfficialAddress() != null) {
            if ((organization.getOfficialAddress().getStreet() == null || 
                 organization.getOfficialAddress().getStreet().trim().isEmpty()) &&
                (organization.getOfficialAddress().getZipCode() == null || 
                 organization.getOfficialAddress().getZipCode().trim().isEmpty())) {
                organization.setOfficialAddress(null);
            }
        }
        
        if (organization.getOfficialAddress() != null) {
            if (organization.getOfficialAddress().getStreet() == null || 
                organization.getOfficialAddress().getStreet().trim().isEmpty()) {
                result.rejectValue("officialAddress.street", "error.officialAddress", 
                    "Если указан официальный адрес, улица обязательна");
            } else if (organization.getOfficialAddress().getStreet().length() > 180) {
                result.rejectValue("officialAddress.street", "error.officialAddress", 
                    "Длина улицы не должна быть больше 180 символов");
            }
            if (organization.getOfficialAddress().getZipCode() == null || 
                organization.getOfficialAddress().getZipCode().trim().isEmpty()) {
                result.rejectValue("officialAddress.zipCode", "error.officialAddress", 
                    "Если указан официальный адрес, почтовый индекс обязателен");
            } else if (organization.getOfficialAddress().getZipCode().length() < 7) {
                result.rejectValue("officialAddress.zipCode", "error.officialAddress", 
                    "Длина почтового индекса должна быть не меньше 7 символов");
            }
        }
        
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.name", "Название обязательно");
        }
        if (organization.getCoordinates() == null || 
            organization.getCoordinates().getX() == null || 
            organization.getCoordinates().getY() == null) {
            result.rejectValue("coordinates", "error.coordinates", "Координаты обязательны");
        } else {
            if (organization.getCoordinates().getY() > 323) {
                result.rejectValue("coordinates.y", "error.coordinates", 
                    "Максимальное значение поля Y: 323");
            }
        }
        if (organization.getPostalAddress() == null || 
            organization.getPostalAddress().getStreet() == null || 
            organization.getPostalAddress().getStreet().trim().isEmpty()) {
            result.rejectValue("postalAddress.street", "error.postalAddress", 
                "Почтовый адрес обязателен");
        } else if (organization.getPostalAddress().getStreet().length() > 180) {
            result.rejectValue("postalAddress.street", "error.postalAddress", 
                "Длина улицы не должна быть больше 180 символов");
        }
        if (organization.getPostalAddress() == null || 
            organization.getPostalAddress().getZipCode() == null || 
            organization.getPostalAddress().getZipCode().trim().isEmpty()) {
            result.rejectValue("postalAddress.zipCode", "error.postalAddress", 
                "Почтовый индекс обязателен");
        } else if (organization.getPostalAddress().getZipCode().length() < 7) {
            result.rejectValue("postalAddress.zipCode", "error.postalAddress", 
                "Длина почтового индекса должна быть не меньше 7 символов");
        }
        if (organization.getAnnualTurnover() == null || organization.getAnnualTurnover() <= 0) {
            result.rejectValue("annualTurnover", "error.annualTurnover", 
                "Годовой оборот должен быть больше 0");
        }
        if (organization.getEmployeesCount() == null || organization.getEmployeesCount() <= 0) {
            result.rejectValue("employeesCount", "error.employeesCount", 
                "Количество сотрудников должно быть больше 0");
        }
        if (organization.getRating() == null || organization.getRating() <= 0) {
            result.rejectValue("rating", "error.rating", "Рейтинг должен быть больше 0");
        }
        if (organization.getType() == null) {
            result.rejectValue("type", "error.type", "Тип организации обязателен");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("existingAddresses", organizationService.getAllAddresses());
            model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
            return "form";
        }
        
        try {
            organizationService.createOrganization(organization);
            redirectAttributes.addFlashAttribute("message", "Организация успешно создана");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при создании организации: " + e.getMessage());
            model.addAttribute("existingAddresses", organizationService.getAllAddresses());
            model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
            return "form";
        }
    }

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model) {
        return organizationService.getOrganizationById(id)
                .map(org -> {
                    model.addAttribute("organization", org);
                    model.addAttribute("existingAddresses", organizationService.getAllAddresses());
                    model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
                    return "form";
                })
                .orElseGet(() -> {
                    return "redirect:/";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateOrganization(@PathVariable Long id,
                                     @ModelAttribute Organization organization,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        normalizeCoordinates(organization, result, request);
        
        if (organization.getOfficialAddress() != null) {
            if ((organization.getOfficialAddress().getStreet() == null || 
                 organization.getOfficialAddress().getStreet().trim().isEmpty()) &&
                (organization.getOfficialAddress().getZipCode() == null || 
                 organization.getOfficialAddress().getZipCode().trim().isEmpty())) {
                organization.setOfficialAddress(null);
            }
        }
        
        if (organization.getOfficialAddress() != null) {
            if (organization.getOfficialAddress().getStreet() == null || 
                organization.getOfficialAddress().getStreet().trim().isEmpty()) {
                result.rejectValue("officialAddress.street", "error.officialAddress", 
                    "Если указан официальный адрес, улица обязательна");
            } else if (organization.getOfficialAddress().getStreet().length() > 180) {
                result.rejectValue("officialAddress.street", "error.officialAddress", 
                    "Длина улицы не должна быть больше 180 символов");
            }
            if (organization.getOfficialAddress().getZipCode() == null || 
                organization.getOfficialAddress().getZipCode().trim().isEmpty()) {
                result.rejectValue("officialAddress.zipCode", "error.officialAddress", 
                    "Если указан официальный адрес, почтовый индекс обязателен");
            } else if (organization.getOfficialAddress().getZipCode().length() < 7) {
                result.rejectValue("officialAddress.zipCode", "error.officialAddress", 
                    "Длина почтового индекса должна быть не меньше 7 символов");
            }
        }
        
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.name", "Название обязательно");
        }
        if (organization.getCoordinates() == null || 
            organization.getCoordinates().getX() == null || 
            organization.getCoordinates().getY() == null) {
            result.rejectValue("coordinates", "error.coordinates", "Координаты обязательны");
        } else {
            if (organization.getCoordinates().getY() > 323) {
                result.rejectValue("coordinates.y", "error.coordinates", 
                    "Максимальное значение поля Y: 323");
            }
        }
        if (organization.getPostalAddress() == null || 
            organization.getPostalAddress().getStreet() == null || 
            organization.getPostalAddress().getStreet().trim().isEmpty()) {
            result.rejectValue("postalAddress.street", "error.postalAddress", 
                "Почтовый адрес обязателен");
        } else if (organization.getPostalAddress().getStreet().length() > 180) {
            result.rejectValue("postalAddress.street", "error.postalAddress", 
                "Длина улицы не должна быть больше 180 символов");
        }
        if (organization.getPostalAddress() == null || 
            organization.getPostalAddress().getZipCode() == null || 
            organization.getPostalAddress().getZipCode().trim().isEmpty()) {
            result.rejectValue("postalAddress.zipCode", "error.postalAddress", 
                "Почтовый индекс обязателен");
        } else if (organization.getPostalAddress().getZipCode().length() < 7) {
            result.rejectValue("postalAddress.zipCode", "error.postalAddress", 
                "Длина почтового индекса должна быть не меньше 7 символов");
        }
        if (organization.getAnnualTurnover() == null || organization.getAnnualTurnover() <= 0) {
            result.rejectValue("annualTurnover", "error.annualTurnover", 
                "Годовой оборот должен быть больше 0");
        }
        if (organization.getEmployeesCount() == null || organization.getEmployeesCount() <= 0) {
            result.rejectValue("employeesCount", "error.employeesCount", 
                "Количество сотрудников должно быть больше 0");
        }
        if (organization.getRating() == null || organization.getRating() <= 0) {
            result.rejectValue("rating", "error.rating", "Рейтинг должен быть больше 0");
        }
        if (organization.getType() == null) {
            result.rejectValue("type", "error.type", "Тип организации обязателен");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("existingAddresses", organizationService.getAllAddresses());
            model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
            return "form";
        }
        
        try {
            organization.setId(id);
            organizationService.updateOrganization(organization);
            redirectAttributes.addFlashAttribute("message", "Организация успешно обновлена");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обновлении организации: " + e.getMessage());
            model.addAttribute("existingAddresses", organizationService.getAllAddresses());
            model.addAttribute("existingCoordinates", organizationService.getAllCoordinates());
            return "form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteOrganization(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (organizationService.deleteOrganization(id)) {
                redirectAttributes.addFlashAttribute("message", "Организация успешно удалена");
            } else {
                redirectAttributes.addFlashAttribute("error", "Организация не найдена");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/";
    }

    private void normalizeCoordinates(Organization organization,
                                      BindingResult result,
                                      HttpServletRequest request) {
        if (organization == null) {
            return;
        }
        Coordinates coordinates = organization.getCoordinates();
        if (coordinates == null) {
            coordinates = new Coordinates();
            organization.setCoordinates(coordinates);
        }
        
        // Парсим X и Y из запроса
        String rawX = request.getParameter("coordinates.x");
        String rawY = request.getParameter("coordinates.y");
        String coordId = request.getParameter("coordinates.id");
        
        Integer newX = coordinates.getX(); 
        Float newY = coordinates.getY();
        
        // Если в параметрах запроса есть значения, они приоритетнее
        if (rawX != null && !rawX.isBlank()) {
            try {
                newX = Integer.valueOf(rawX.trim());
            } catch (NumberFormatException ex) {
                result.rejectValue("coordinates.x", "error.coordinates", "Некорректное значение X");
                return; // Прерываем обработку при ошибке парсинга
            }
        }
        
        if (rawY != null && !rawY.isBlank()) {
            try {
                newY = Float.valueOf(rawY.trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                result.rejectValue("coordinates.y", "error.coordinates", "Некорректное значение Y");
                return; // Прерываем обработку при ошибке парсинга
            }
        }
        
        // Если есть ID существующих координат, проверяем, изменились ли значения
        if (coordId != null && !coordId.isBlank() && !coordId.equals("new") && !coordId.equals("")) {
            try {
                Long existingCoordId = Long.valueOf(coordId.trim());
                Coordinates existingCoord = organizationService.getCoordinatesById(existingCoordId);
                
                if (existingCoord != null) {
                    // Если X и Y не заполнены (null), значит пользователь выбрал существующие координаты из списка
                    if (newX == null && newY == null) {
                        organization.setCoordinates(existingCoord);
                        return;
                    }
                    
                    // Проверяем, совпадают ли новые значения с существующими
                    boolean xChanged = newX != null && !newX.equals(existingCoord.getX());
                    boolean yChanged = newY != null && !newY.equals(existingCoord.getY());
                    
                    if (!xChanged && !yChanged) {
                        // Значения не изменились, используем существующие координаты
                        organization.setCoordinates(existingCoord);
                        return;
                    }
                    // Если значения изменились, создаём новые координаты
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        // Создаём новый объект координат с новыми значениями
        Coordinates newCoordinates = new Coordinates();
        if (newX != null) {
            newCoordinates.setX(newX);
        }
        if (newY != null) {
            newCoordinates.setY(newY);
        }
        organization.setCoordinates(newCoordinates);
    }
}
