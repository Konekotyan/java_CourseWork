package ru.mirea.estore.contoller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ru.mirea.estore.dao.ProductDAO;
import ru.mirea.estore.dao.OrderDAO;
import ru.mirea.estore.entity.Product;
import ru.mirea.estore.form.CustomerForm;
import ru.mirea.estore.model.StoreInfo;
import ru.mirea.estore.model.ProductInfo;
import ru.mirea.estore.model.CustomerInfo;
import ru.mirea.estore.pagination.PaginationResult;
import ru.mirea.estore.utils.Utils;
import ru.mirea.estore.validator.CustomerFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class MainController {

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CustomerFormValidator customerFormValidator;

    @InitBinder
    public void myInitBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);

        // Case update quantity in cart
        //(@ModelAttribute("storeForm") @Validated StoreInfo storeForm)
        if (target.getClass() == StoreInfo.class) {

        }

        // Case save customer information.
        // (@ModelAttribute @Validated CustomerInfo customerForm)
        else if (target.getClass() == CustomerForm.class) {
            dataBinder.setValidator(customerFormValidator);
        }

    }


    @RequestMapping("/403")
    public String accessDenied() {
        return "/403";
    }

    @RequestMapping("/")
    public String home() {
        return "index";
    }

    // Product List
    @RequestMapping({ "/productList" })
    public String listProductHandler(Model model, //
                                     @RequestParam(value = "name", defaultValue = "") String likeName,
                                     @RequestParam(value = "page", defaultValue = "1") int page) {
        final int maxResult = 7;
        final int maxNavigationPage = 10;

        PaginationResult<ProductInfo> result = productDAO.queryProducts(page, //
                maxResult, maxNavigationPage, likeName);

        model.addAttribute("paginationProducts", result);
        return "productList";
    }

    @RequestMapping({ "/buyProduct" })
    public String listProductHandler(HttpServletRequest request, Model model, //
                                     @RequestParam(value = "code", defaultValue = "") String code) {

        Product product = null;
        if (code != null && code.length() > 0) {
            product = productDAO.findProduct(code);
        }
        if (product != null) {

            //
            StoreInfo storeInfo = Utils.getStoreInSession(request);

            ProductInfo productInfo = new ProductInfo(product);

            storeInfo.addProduct(productInfo, 1);
        }

        return "redirect:/estore";
    }

    @RequestMapping({ "/estoreRemoveProduct" })
    public String removeProductHandler(HttpServletRequest request, Model model, //
                                       @RequestParam(value = "code", defaultValue = "") String code) {
        Product product = null;
        if (code != null && code.length() > 0) {
            product = productDAO.findProduct(code);
        }
        if (product != null) {

            StoreInfo storeInfo = Utils.getStoreInSession(request);

            ProductInfo productInfo = new ProductInfo(product);

            storeInfo.removeProduct(productInfo);

        }

        return "redirect:/estore";
    }

    // POST: Update quantity for product in store
    @RequestMapping(value = { "/estore" }, method = RequestMethod.POST)
    public String estoreUpdateQty(HttpServletRequest request, //
                                        Model model, //
                                        @ModelAttribute("storeForm") StoreInfo storeForm) {

        StoreInfo storeInfo = Utils.getStoreInSession(request);
        storeInfo.updateQuantity(storeForm);

        return "redirect:/estore";
    }

    // GET: Show cart.

    @RequestMapping(value = { "/estore" }, method = RequestMethod.GET)
    public String estoreHandler(HttpServletRequest request, Model model) {
        return doIt1(request, model);
    }

    private String doIt1(HttpServletRequest request, Model model){
        StoreInfo myStore = Utils.getStoreInSession(request);

        model.addAttribute("storeForm", myStore);
        return "estore";
    }

    // GET: Enter customer information.

    @RequestMapping(value = { "/estoreCustomer" }, method = RequestMethod.GET)
    public String estoreCustomerForm(HttpServletRequest request, Model model) {

        StoreInfo storeInfo = Utils.getStoreInSession(request);

        if (storeInfo.isEmpty()) {

            return "redirect:/estore";
        }
        CustomerInfo customerInfo = storeInfo.getCustomerInfo();

        CustomerForm customerForm = new CustomerForm(customerInfo);

        model.addAttribute("customerForm", customerForm);

        return "estoreCustomer";
    }

    // POST: Save customer information.

    @RequestMapping(value = { "/estoreCustomer" }, method = RequestMethod.POST)
    public String estoreCustomerSave(HttpServletRequest request, //
                                           Model model, //
                                           @ModelAttribute("customerForm") @Validated CustomerForm customerForm, //
                                           BindingResult result, //
                                           final RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            customerForm.setValid(false);
            // Forward to reenter customer info.
            return "estoreCustomer";
        }

        customerForm.setValid(true);
        StoreInfo storeInfo = Utils.getStoreInSession(request);
        CustomerInfo customerInfo = new CustomerInfo(customerForm);
        storeInfo.setCustomerInfo(customerInfo);

        return "redirect:/estoreConfirm";
    }

    // GET: Show information to confirm.

    @RequestMapping(value = { "/estoreConfirm" }, method = RequestMethod.GET)
    public String estoreConfirmReview(HttpServletRequest request, Model model) {
        StoreInfo storeInfo = Utils.getStoreInSession(request);

        if (storeInfo.isEmpty()) {

            return "redirect:/estore";
        } else if (storeInfo.isValidCustomer()) {

            return "redirect:/estoreCustomer";
        }
        model.addAttribute("myStore", storeInfo);

        return "estoreConfirm";
    }

    // POST: Submit Cart (Save)
    @RequestMapping(value = { "/estoreConfirm" }, method = RequestMethod.POST)

    public String estoreConfirmSave(HttpServletRequest request, Model model) {
        StoreInfo storeInfo = Utils.getStoreInSession(request);

        if (storeInfo.isEmpty()) {

            return "redirect:/estore";
        } else if (storeInfo.isValidCustomer()) {

            return "redirect:/estoreCustomer";
        }
        try {
            orderDAO.saveOrder(storeInfo);
        } catch (Exception e) {

            return "estoreConfirm";
        }

        // Remove Store from Session.
        Utils.removeStoreInSession(request);

        // Store last cart.
        Utils.storeLastOrderedStoreInSession(request, storeInfo);

        return "redirect:/estoreEnd";
    }

    @RequestMapping(value = { "/estoreEnd" }, method = RequestMethod.GET)
    public String estoreEnd(HttpServletRequest request, Model model) {

        StoreInfo lastOrderedStore = Utils.getLastOrderedStoreInSession(request);

        if (lastOrderedStore == null) {
            return "redirect:/estore";
        }
        model.addAttribute("lastOrderedStore", lastOrderedStore);
        return "estoreEnd";
    }

    @RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
    public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
                             @RequestParam("code") String code) throws IOException {
        Product product = null;
        if (code != null) {
            product = this.productDAO.findProduct(code);
        }
        if (product != null && product.getImage() != null) {
            response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
            response.getOutputStream().write(product.getImage());
        }
        response.getOutputStream().close();
    }

}