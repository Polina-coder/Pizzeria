package by.lobovich.delivery.controller;

import by.lobovich.delivery.entity.CreditCard;
import by.lobovich.delivery.entity.Order;
import by.lobovich.delivery.entity.PersonalInfo;
import by.lobovich.delivery.entity.User;
import by.lobovich.delivery.service.OrderService;
import by.lobovich.delivery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return userService.getUserFromSecurityContext();
    }

    @ModelAttribute("currentOrder")
    public Order getCurrentOrder() {
        return orderService.getLastByUser(getCurrentUser());
        //return getCurrentUser().getOrder();
    }

    @ModelAttribute("currentPersonalInfo")
    public PersonalInfo currentPersonalInfo() {
        return getCurrentUser().getPersonalInfo();
    }

    @GetMapping("profile")
    public String profile() {
        return "header";
    }

    @GetMapping("{id}")
    public String viewProfile(@PathVariable Long id) {
        User user = userService.getById(id);
        User currentUser = userService.getUserFromSecurityContext();
        if (user == null) {
            return "redirect:/home";
        }
        if (!user.equals(currentUser)) {
            return "redirect:/user/" + currentUser.getId();
        }
        PersonalInfo personalInfo = user.getPersonalInfo();
        if (personalInfo == null) {
            personalInfo = new PersonalInfo();
        }
        Set<CreditCard> creditCards = personalInfo.getCreditCards();
        if (creditCards == null) {
            creditCards = Collections.emptySet();
        }
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String profileEdit() {
        return "profileEdit";
    }

    @PostMapping("/profile/edit")
    public String updateUser(
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phoneNumber,
            @RequestParam String email
    ) {
        User user = userService.getUserFromSecurityContext();
        PersonalInfo personalInfo = user.getPersonalInfo();
        personalInfo.setFirstName(firstName);
        personalInfo.setLastName(lastName);
        personalInfo.setPhoneNumber(phoneNumber);
        personalInfo.setEmail(email);
        user.setPersonalInfo(personalInfo);
        if (!user.getUsername().equals(username)) {
            user.setUsername(username);
            userService.update(user);
            return "redirect:/login";
        }
        userService.update(user);
        return "redirect:/user/" + user.getId();
    }

}
