package com.example.car_park.controllers;

import com.example.car_park.dao.model.Brand;
import com.example.car_park.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping("")
    public String getBrands(Model model) {
        model.addAttribute("brands", brandService.findAll());
        return "brands";
    }

    @GetMapping("/new")
    public String createBrand(Model model) {
        model.addAttribute("brand", new Brand());
        return "new_brand";
    }

    @PostMapping("/save")
    public String saveBrand(@ModelAttribute Brand brand) {
        brandService.save(brand);
        return "redirect:/brands";
    }

    @GetMapping("/edit")
    public String editBrand(@RequestParam Long id, Model model) {
        Brand brand = brandService.find(id);
        if (brand == null) {
            return "redirect:/brands";
        }

        model.addAttribute("brand", brand);
        return "edit_brand";
    }

    @GetMapping("/delete")
    public String deleteBrand(@RequestParam Long id) {
        brandService.delete(id);
        return "redirect:/brands";
    }

    @GetMapping("/search")
    public String searchBrand(@RequestParam String keyword, Model model) {
        model.addAttribute("brands", brandService.findByKeyword(keyword));
        return "search_brands";
    }
}