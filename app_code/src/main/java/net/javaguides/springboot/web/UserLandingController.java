package net.javaguides.springboot.web;

import net.javaguides.springboot.model.TemporaryUser;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.service.UserService;
import net.javaguides.springboot.web.dto.UserEmailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/welcome")
public class UserLandingController
{

    private UserService userService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public UserLandingController(UserService userService)
    {
        super();
        this.userService = userService;
    }

    @ModelAttribute("user")
    public UserEmailDto userEmailDto()
    {
        return new UserEmailDto();
    }

    @GetMapping
    public String showRegistrationLanding(HttpServletRequest httpServletRequest)
    {
        if (httpServletRequest.isUserInRole("ROLE_USER")) {
            return "redirect:/home";
        }
        return "welcome";
    }

    @PostMapping
    public String registerUserAccountTemp(@ModelAttribute("user") UserEmailDto userEmailDto, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        try {
            TemporaryUser temporaryUser = userService.saveEmail(userEmailDto, redirectAttributes);
            User user = userService.findByEmail(userEmailDto.getEmail());
            if(temporaryUser == null || user != null) {
                redirectAttributes.addFlashAttribute("error", "User already exists");
                return "redirect:/welcome";
            }
            else {

                String regexp = "^[A-Za-z0-9+_.-]+@(.+)$";
                Pattern p = Pattern.compile(regexp);
                Matcher m = p.matcher(userEmailDto.getEmail());
                if(!m.matches())
                {
                    redirectAttributes.addAttribute("error3", "Email has a wrong form, please check it and enter a correct one");
                    redirectAttributes.addAttribute("reg", "");
                    redirectAttributes.addAttribute("email", userEmailDto.getEmail());
                    return "redirect:/welcome";
                }
                String appUrl = request.getContextPath();
                eventPublisher.publishEvent(new OnRegistrationCompleteEvent(temporaryUser,
                        request.getLocale(), appUrl));
            }
        }
        catch(DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "User already exists");
            return "redirect:/welcome";
        }
        redirectAttributes.addFlashAttribute("success", "Email has been sent.");
        return "redirect:/login";
    }
}
