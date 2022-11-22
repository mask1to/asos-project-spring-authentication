package net.javaguides.springboot.service;

import java.util.*;

import net.javaguides.springboot.model.TemporaryUser;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.model.VerificationToken;
import net.javaguides.springboot.repository.RoleRepository;
import net.javaguides.springboot.repository.TemporaryUserRepository;
import net.javaguides.springboot.repository.VerificationTokenRepository;
import net.javaguides.springboot.web.dto.UserEmailDto;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import net.javaguides.springboot.model.Role;
import net.javaguides.springboot.repository.UserRepository;
import net.javaguides.springboot.web.dto.UserRegistrationDto;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private TemporaryUserRepository temporaryUserRepository;

    public static String QR_PREFIX =
            "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    public static String APP_NAME = "TP";

    @Value("${admin.password}")
    private String admin_password;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, TemporaryUserRepository temporaryUserRepository, RoleRepository roleRepository) {
        super();
        this.userRepository = userRepository;
        this.temporaryUserRepository = temporaryUserRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User save(UserRegistrationDto registrationDto, String authyId) {
        Role pre_user = createRoleIfNotFound("ROLE_PRE_USER");
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setUsingfa(registrationDto.isUsingfa());
        user.setRoles(new HashSet<>(Arrays.asList(pre_user)));
        user.setPhoneCode(registrationDto.getPhoneNumber_phoneCode());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setAuthyId(authyId);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public User save(User user) {
        Set<Role> roles = new HashSet<>();

        User userEdit = userRepository.findUserById(user.getId());

        for (Role role : user.getRoles()) {
            Role newRole = createRoleIfNotFound(role.getName());
            roles.add(newRole);
        }

        userEdit.setRoles(roles);
        return userRepository.save(userEdit);
    }

    @Override
    public TemporaryUser saveEmail(UserEmailDto userEmailDto, RedirectAttributes redirectAttributes) {
        try {
            TemporaryUser temporaryUser = new TemporaryUser(userEmailDto.getEmail());
            return temporaryUserRepository.save(temporaryUser);
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "User already exists");
            return null;
        }

    }

    @Override
    public TemporaryUser getTemporaryUser(String verificationToken) {
        TemporaryUser temporaryUser = tokenRepository.findByToken(Optional.ofNullable(verificationToken)).getTemporaryUser();
        return temporaryUser;
    }

    @Override
    public TemporaryUser getTemporaryUserByMail(String mail) {
        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(mail);
        return temporaryUser;
    }

    @Override
    public void removeByMail(String mail) {
        TemporaryUser temporaryUser = temporaryUserRepository.findByEmail(mail);
        temporaryUserRepository.delete(temporaryUser);
    }

    @Override
    public VerificationToken getVerificationToken(Optional<String> VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    @Override
    public void createVerificationToken(TemporaryUser temporaryUser, String token) {
        VerificationToken myToken = new VerificationToken(token, temporaryUser);
        tokenRepository.save(myToken);
    }

    @Override
    public void saveRegisteredUser(TemporaryUser temporaryUser) {
        temporaryUserRepository.save(temporaryUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid Email or password");
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>(); // use list if you wish
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }

    /*private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName()));
    }*/

    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Override
    public User findUserById(Long id) {
        User user = userRepository.findUserById(id);
        return user;
    }

    @Override
    public boolean checkcode(String secretCode, String code) {
        Totp totp = new Totp(secretCode);
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }

        if (totp.verify(code)) {
            return true;
        }
        return false;
    }

    @Override
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<Role> allRoles() {
        return roleRepository.findAll();
    }

    @PostConstruct
    private void postConstruct() {
        if (userRepository.findByEmail("rosina.jakub@gmail.com") == null) {
            Role admin = createRoleIfNotFound("ROLE_ADMIN");
            Role pre_user = createRoleIfNotFound("ROLE_PRE_USER");
            User user = new User();
            user.setFirstName("ADMIN");
            user.setLastName("ADMIN");
            user.setEmail("rosina.jakub@gmail.com");
            user.setPassword(passwordEncoder.encode(admin_password));
            user.setRoles(new HashSet<>(Arrays.asList(admin, pre_user)));
            user.setUsingfa(true);
            user.setPhoneCode("+421");
            user.setPhoneNumber("910278653");
            user.setAuthyId("44401362");
            user.setEnabled(true);
            userRepository.save(user);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, noRollbackFor = Exception.class)
    public Role createRoleIfNotFound(String name) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
        }
        return role;
    }

}
