package net.javaguides.springboot.web;

import com.authy.AuthyApiClient;
import com.authy.AuthyException;
import com.authy.api.Users;
import eu.bitwalker.useragentutils.UserAgent;
import net.javaguides.springboot.model.TemporaryUser;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.model.VerificationToken;
import net.javaguides.springboot.repository.TemporaryUserRepository;
import net.javaguides.springboot.repository.VerificationTokenRepository;
import net.javaguides.springboot.service.UserService;
import net.javaguides.springboot.web.dto.UserRegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;


@Controller
public class UserRegistrationController {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    private UserService userService;
    private TemporaryUserRepository temporaryUserRepository;
    private VerificationTokenRepository verificationTokenRepository;
    private UserRegistrationDto registrationDto;

    @Value( "${authy.api}" )
    private String API_KEY;

    public UserRegistrationController(UserService userService, TemporaryUserRepository temporaryUserRepository, VerificationTokenRepository verificationTokenRepository) {
        super();
        this.userService = userService;
        this.temporaryUserRepository = temporaryUserRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @ModelAttribute("user")
    public UserRegistrationDto UserRegistrationDto() {
        return new UserRegistrationDto();
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String showRegistrationForm(HttpServletRequest httpServletRequest, @RequestParam("token") Optional<String> token, Model model) throws ServletException {
        VerificationToken verificationToken = userService.getVerificationToken(token);

        if (httpServletRequest.isUserInRole("ROLE_USER")) {
            return "redirect:/home";
        } else if (verificationToken == null) {
            return "/badToken";
        }
        else if (httpServletRequest.isUserInRole("ROLE_PRE_USER")){
            httpServletRequest.logout();
            return "redirect:/";
        }

        TemporaryUser temporaryUser = verificationToken.getTemporaryUser();
        Calendar cal = Calendar.getInstance();
        if (temporaryUser.isEnabled() == false) {
            if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
                return "/badToken";
            }

            temporaryUser.setEnabled(true);
            userService.saveRegisteredUser(temporaryUser);
            model.addAttribute("reg", "");
            model.addAttribute("token", verificationToken.getToken());
            model.addAttribute("email", verificationToken.getTemporaryUser().getEmail());
            return null;
        }
        model.addAttribute("reg", "");
        model.addAttribute("token", verificationToken.getToken());
        model.addAttribute("email", verificationToken.getTemporaryUser().getEmail());
        return null;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST, params = "register")
    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto,BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {

        if(bindingResult.hasErrors()){
            return "/registration";
        }

        User user = userService.findByEmail(registrationDto.getEmail());;

        if (user != null) {
            model.addAttribute("error", "User already exists");
            model.addAttribute("token", registrationDto.getToken());
            model.addAttribute("email", registrationDto.getEmail());
            model.addAttribute("reg", "");
            return "/registration";
        }

        if(registrationDto.getFirstName().length() < 2 || registrationDto.getFirstName().length() > 30)
        {
            model.addAttribute("error1", "First name length has to be 2-30 characters long");
            model.addAttribute("reg", "");
            model.addAttribute("token", registrationDto.getToken());
            model.addAttribute("email", registrationDto.getEmail());
            return "/registration";
        }

        if(registrationDto.getLastName().length() < 2 || registrationDto.getLastName().length() > 30)
        {
            model.addAttribute("error2", "Last name length has to be 2-30 characters long");
            model.addAttribute("reg", "");
            model.addAttribute("token", registrationDto.getToken());
            model.addAttribute("email", registrationDto.getEmail());
            return "/registration";
        }

        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);

        if(!registrationDto.getPassword().matches(regex))
        {
            model.addAttribute("error4", "Password conditions: \n" +
                                               "- min. 8 characters long\n" +
                                               "- 1 upper alpha char \n" +
                                               "- 1 lower alpha char\n" +
                                               "- 1 digit\n" +
                                               "- 1 special char");
            model.addAttribute("reg", "");
            model.addAttribute("token", registrationDto.getToken());
            model.addAttribute("email", registrationDto.getEmail());
            return "/registration";
        }

        if(registrationDto.getPhoneNumber().length() < 5 || registrationDto.getPhoneNumber().length() > 11 || !registrationDto.getPhoneNumber().matches("[0-9]+"))
        {
            model.addAttribute("error5", "Phone number needs to have atleast 6 and at most 11 digits");
            model.addAttribute("reg", "");
            model.addAttribute("token", registrationDto.getToken());
            model.addAttribute("email", registrationDto.getEmail());
            return "/registration";
        }

        if (registrationDto.isUsingfa())
        {
            model.addAttribute("gaReg", "");
            this.registrationDto = registrationDto;
            return "/registration";
        } else {

            if (userService.save(registrationDto, null) != null) {
                temporaryUserRepository.deleteTemporaryUserByEmail(registrationDto.getEmail());
                redirectAttributes.addFlashAttribute("success", "Registration was successful. You can log in!");

                return "redirect:/login";
            } else {
                model.addAttribute("reg", "");
                model.addAttribute("email", registrationDto.getEmail());
                return "/registration";
            }
        }
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST, params = "registerGa")
    public String registerGaAccount(@ModelAttribute("user") UserRegistrationDto registrationDto, RedirectAttributes redirectAttributes, Model model) throws UnsupportedEncodingException, AuthyException {

        AuthyApiClient client = new AuthyApiClient(API_KEY);

        Users users = client.getUsers();
        com.authy.api.User user = users.createUser(this.registrationDto.getEmail(), this.registrationDto.getPhoneNumber(), this.registrationDto.getPhoneNumber_phoneCode());

        if (user.isOk()) {
            userService.save(this.registrationDto, String.valueOf(user.getId()));
            temporaryUserRepository.deleteTemporaryUserByEmail(this.registrationDto.getEmail());
            redirectAttributes.addFlashAttribute("success", "Registration was successful. You can log in!");
            return "redirect:/login";
        } else {
            model.addAttribute("error", user.getError().getMessage());
            model.addAttribute("gaReg", "");
            return "/registration";
        }

    }
}
