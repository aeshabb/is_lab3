package org.itmo.lab3.controller;

import org.itmo.lab3.model.Address;
import org.itmo.lab3.model.Organization;
import org.itmo.lab3.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/special")
public class SpecialOperationsController {

    private final OrganizationService organizationService;

    @Autowired
    public SpecialOperationsController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public String specialOperationsPage(Model model) {
        return "special";
    }

    @PostMapping("/delete-by-rating")
    public String deleteOrganizationsByRating(@RequestParam Double rating,
                                               RedirectAttributes redirectAttributes) {
        try {
            int count = organizationService.deleteOrganizationsByRating(rating);
            redirectAttributes.addFlashAttribute("message", 
                    "Удалено организаций: " + count);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Ошибка: " + e.getMessage());
        }
        return "redirect:/special";
    }

    /**
     * Вернуть массив объектов, значение поля name которых содержит заданную подстроку
     */
    @PostMapping("/find-by-name-substring")
    public String findOrganizationsByNameSubstring(@RequestParam String nameSubstring, Model model) {
        try {
            List<Organization> organizations = organizationService.findOrganizationsByNameSubstring(nameSubstring);
            model.addAttribute("result", organizations);
            model.addAttribute("operation", "findByNameSubstring");
            model.addAttribute("searchValue", nameSubstring);
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "special";
    }

    /**
     * Вернуть массив уникальных значений поля postalAddress по всем объектам
     */
    @GetMapping("/get-unique-postal-addresses")
    public String getUniquePostalAddresses(Model model) {
        try {
            List<Address> addresses = organizationService.getUniquePostalAddresses();
            model.addAttribute("addresses", addresses);
            model.addAttribute("operation", "getUniquePostalAddresses");
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "special";
    }

    /**
     * Добавить нового сотрудника в организацию с указанным id
     */
    @PostMapping("/add-employee")
    public String addEmployeeToOrganization(@RequestParam Long organizationId,
                                            RedirectAttributes redirectAttributes) {
        try {
            Organization organization = organizationService.addEmployeeToOrganization(organizationId);
            redirectAttributes.addFlashAttribute("message", 
                    "Сотрудник добавлен в организацию " + organization.getName() + 
                    " (ID: " + organization.getId() + "). Текущее количество сотрудников: " + 
                    organization.getEmployeesCount());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Ошибка: " + e.getMessage());
        }
        return "redirect:/special";
    }

    /**
     * Реализовать поглощение одной организацией другой
     */
    @PostMapping("/merge-organizations")
    public String mergeOrganizations(@RequestParam Long targetOrganizationId,
                                     @RequestParam Long sourceOrganizationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Organization target = organizationService.mergeOrganizations(targetOrganizationId, sourceOrganizationId);
            redirectAttributes.addFlashAttribute("message", 
                    "Организация " + sourceOrganizationId + " поглощена организацией " + 
                    target.getName() + " (ID: " + target.getId() + "). Количество сотрудников объединено.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Ошибка: " + e.getMessage());
        }
        return "redirect:/special";
    }
}
