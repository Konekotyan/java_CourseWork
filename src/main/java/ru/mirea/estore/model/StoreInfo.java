package ru.mirea.estore.model;

import java.util.ArrayList;
import java.util.List;

public class StoreInfo {

    private int orderNum;

    private CustomerInfo customerInfo;

    private final List<StoreLineInfo> cartLines = new ArrayList<StoreLineInfo>();

    public StoreInfo() {

    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public List<StoreLineInfo> getCartLines() {
        return this.cartLines;
    }

    private StoreLineInfo findLineByCode(String code) {
        for (StoreLineInfo line : this.cartLines) {
            if (line.getProductInfo().getCode().equals(code)) {
                return line;
            }
        }
        return null;
    }

    public void addProduct(ProductInfo productInfo, int quantity) {
        StoreLineInfo line = this.findLineByCode(productInfo.getCode());

        if (line == null) {
            line = new StoreLineInfo();
            line.setQuantity(0);
            line.setProductInfo(productInfo);
            this.cartLines.add(line);
        }
        int newQuantity = line.getQuantity() + quantity;
        if (newQuantity <= 0) {
            this.cartLines.remove(line);
        } else {
            line.setQuantity(newQuantity);
        }
    }

    public void validate() {

    }

    public void updateProduct(String code, int quantity) {
        StoreLineInfo line = this.findLineByCode(code);

        if (line != null) {
            if (quantity <= 0) {
                this.cartLines.remove(line);
            } else {
                line.setQuantity(quantity);
            }
        }
    }

    public void removeProduct(ProductInfo productInfo) {
        StoreLineInfo line = this.findLineByCode(productInfo.getCode());
        if (line != null) {
            this.cartLines.remove(line);
        }
    }

    public boolean isEmpty() {
        return this.cartLines.isEmpty();
    }

    public boolean isValidCustomer() {
        return this.customerInfo != null && this.customerInfo.isValid();
    }

    public int getQuantityTotal() {
        int quantity = 0;
        for (StoreLineInfo line : this.cartLines) {
            quantity += line.getQuantity();
        }
        return quantity;
    }

    public double getAmountTotal() {
        double total = 0;
        for (StoreLineInfo line : this.cartLines) {
            total += line.getAmount();
        }
        return total;
    }

    public void updateQuantity(StoreInfo cartForm) {
        if (cartForm != null) {
            List<StoreLineInfo> lines = cartForm.getCartLines();
            for (StoreLineInfo line : lines) {
                this.updateProduct(line.getProductInfo().getCode(), line.getQuantity());
            }
        }

    }

}
