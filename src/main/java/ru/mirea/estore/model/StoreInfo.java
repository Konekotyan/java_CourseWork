package ru.mirea.estore.model;

import java.util.ArrayList;
import java.util.List;

public class StoreInfo {

    private int orderNum;

    private CustomerInfo customerInfo;

    private final List<StoreLineInfo> storeLines = new ArrayList<StoreLineInfo>();

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

    public List<StoreLineInfo> getStoreLines() {
        return this.storeLines;
    }

    private StoreLineInfo findLineByCode(String code) {
        for (StoreLineInfo line : this.storeLines) {
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
            this.storeLines.add(line);
        }
        int newQuantity = line.getQuantity() + quantity;
        if (newQuantity <= 0) {
            this.storeLines.remove(line);
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
                this.storeLines.remove(line);
            } else {
                line.setQuantity(quantity);
            }
        }
    }

    public void removeProduct(ProductInfo productInfo) {
        StoreLineInfo line = this.findLineByCode(productInfo.getCode());
        if (line != null) {
            this.storeLines.remove(line);
        }
    }

    public boolean isEmpty() {
        return this.storeLines.isEmpty();
    }

    public boolean isValidCustomer() {
        return this.customerInfo == null || !this.customerInfo.isValid();
    }

    public int getQuantityTotal() {
        int quantity = 0;
        for (StoreLineInfo line : this.storeLines) {
            quantity += line.getQuantity();
        }
        return quantity;
    }

    public double getAmountTotal() {
        double total = 0;
        for (StoreLineInfo line : this.storeLines) {
            total += line.getAmount();
        }
        return total;
    }

    public void updateQuantity(StoreInfo storeForm) {
        if (storeForm != null) {
            List<StoreLineInfo> lines = storeForm.getStoreLines();
            for (StoreLineInfo line : lines) {
                this.updateProduct(line.getProductInfo().getCode(), line.getQuantity());
            }
        }

    }

}
