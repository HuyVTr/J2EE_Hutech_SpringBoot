package fit.hutech.spring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.repositories.IBookRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final IBookRepository bookRepository;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "home/index/index";
    }

    @GetMapping("/contact")
    public String contact() {
        return "home/contact";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}