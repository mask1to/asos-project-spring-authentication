package net.javaguides.springboot.web;

import com.authy.AuthyApiClient;
import com.authy.AuthyException;
import com.authy.api.ApprovalRequestParams;
import com.authy.api.OneTouchResponse;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@EnableAsync
public class AuthyNotificationLoginController {
    @Autowired
    ApplicationEventPublisher eventPublisher;

    private UserService userService;

    @Value( "${authy.api}" )
    private String API_KEY;

    public AuthyNotificationLoginController(UserService userService) {
        super();
        this.userService = userService;
    }

    @RequestMapping(value = "/authyNotificationLogin", method = RequestMethod.GET)
    public String showPage(HttpServletRequest httpServletRequest, RedirectAttributes redirectAttributes, Model model) throws AuthyException, InterruptedException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());

        if (httpServletRequest.isUserInRole("ROLE_PRE_USER") && user.getUsingfa()) {
            return null;
        }
        else if (httpServletRequest.isUserInRole("ROLE_USER")) {
            return "redirect:/home";
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/authyNotificationLogin", method = RequestMethod.POST)
    public String notification(RedirectAttributes redirectAttributes, Model model, HttpServletRequest httpServletRequest) throws AuthyException, InterruptedException {
        System.out.println("ddddd" + redirectAttributes);
        model.addAttribute("disabled", true);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        AuthyApiClient client = new AuthyApiClient(API_KEY);

        String message = "Login requested for a TP app.";

        ApprovalRequestParams approvalRequestParams = new ApprovalRequestParams.Builder(Integer.valueOf(user.getAuthyId()), message)
                .addDetail("Email", user.getEmail())
                .addDetail("Phone number", "+" + user.getPhoneCode() + user.getPhoneNumber())
                .setSecondsToExpire(180L)
                .build();

        OneTouchResponse response = client.getOneTouch().sendApprovalRequest(approvalRequestParams);

        if (response.isOk()) {
            String uuid = response.getApprovalRequest().getUUID();
            OneTouchResponse responseNotification = client.getOneTouch().getApprovalRequestStatus(uuid);
            while(!responseNotification.getApprovalRequest().getStatus().equals("approved")) {
                responseNotification = client.getOneTouch().getApprovalRequestStatus(uuid);
                Thread.sleep(1000);

                if(responseNotification.getApprovalRequest().getStatus().equals("expired") || responseNotification.getApprovalRequest().getStatus().equals("denied")) {
                    redirectAttributes.addFlashAttribute("error", "Notification expired or was denied!");
                    return "redirect:/authyNotificationLogin";
                }
            }
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", response.getError());
            return "redirect:/authyNotificationLogin";
        }
    }
}

