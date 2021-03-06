package ImageHoster.controller;

import ImageHoster.model.Image;
import ImageHoster.model.User;
import ImageHoster.model.UserProfile;
import ImageHoster.service.ImageService;
import ImageHoster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    private static final String PASSWORD_PATTERN = "((?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).*)";

    /**
     * Controller method called when the request pattern is of type 'users/registration'
     * and the incoming request is of 'GET' type
     * @param model Model to supply attributes ('User') used for rendering view ('users/registration')
     * @return      'users/registration.html' file, a registration page where a user registers his/ her User details
     */
    @RequestMapping("users/registration")
    public String registration(Model model) {
        User user = new User();
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        model.addAttribute("User", user);
        return "users/registration";
    }

    /**
     * Controller method called when the request pattern is of type 'users/registration'
     * and the incoming request is of 'POST' type
     * Persists User details in database
     * @param user  User model object which has the user's name, password and his/ her profile details
     * @param model Model to supply attributes ('passwordTypeError', 'User') used for rendering view ('users/registration')
     *              in case the password does not meet the specific strength
     * @return      redirects to '/users/login' for user to login
     */
    @RequestMapping(value = "users/registration", method = RequestMethod.POST)
    public String registerUser(User user, Model model) {
        boolean isValidPassword = isValidPassword(user.getPassword());
        if(!isValidPassword) {
            String error = "Password must contain atleast 1 alphabet, 1 number & 1 special character";
            model.addAttribute("passwordTypeError",error);
            model.addAttribute("User", user);
            return "users/registration";
        }
        userService.registerUser(user);
        return "users/login";
    }

    /**
     * Controller method called when the request pattern is of type 'users/login'
     * and the incoming request is of 'GET' type
     * @return 'users/login.html' file for user to login with valid credentials
     */
    @RequestMapping("users/login")
    public String login() {
        return "users/login";
    }

    /**
     * Controller method called when the request pattern is of type 'users/login'
     * and also the incoming request is of POST type
     * User login into application if the username and password provided in the login page exists in the database
     * and sets the logged user details in session
     * @param user      User details entered in the login page
     * @param session   HttpSession to store attributes in HttpSession
     * @return          'users/login.html' if the credentials provided are invalid
     *                  redirects to '/images', which is user home-page, if the credentials provided are valid
     */
    @RequestMapping(value = "users/login", method = RequestMethod.POST)
    public String loginUser(User user, HttpSession session) {
        User existingUser = userService.login(user);
        if (existingUser != null) {
            session.setAttribute("loggeduser", existingUser);
            return "redirect:/images";
        } else {
            return "users/login";
        }
    }

    /**
     * Controller method called when the request pattern is of type 'users/logout'
     * and also the incoming request is of 'POST' type
     * @param model     Model to supply attributes ('images') used for rendering view ('index')
     * @param session   HttpSession to be invalidated once the user logout.
     * @return          'index.html' file which is the landing page of the application
     *                  and displays all the images in the application
     */
    @RequestMapping(value = "users/logout", method = RequestMethod.POST)
    public String logout(Model model, HttpSession session) {
        session.invalidate();

        List<Image> images = imageService.getAllImages();
        model.addAttribute("images", images);
        return "index";
    }

    /**
     * Method to check if the password meets the specific strength
     * The password entered by the user must contain at least 1 alphabet (a-z or A-Z), 1 number (0-9)
     * and 1 special character (any character other than a-z, A-Z and 0-9)
     * @param password  String that represents the password entered by the user
     * @return          boolean
     */
    public boolean isValidPassword(String password) {
        if (password.isEmpty() || password.length() < 3) {
            return false;
        }
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
