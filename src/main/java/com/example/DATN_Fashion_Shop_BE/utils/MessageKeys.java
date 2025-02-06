package com.example.DATN_Fashion_Shop_BE.utils;

public class MessageKeys {
    public static final String INSERT_CATEGORY_SUCCESSFULLY = "category.create_category.create_successfully";
    public static final String INSERT_CATEGORY_FAILED = "category.create_category.create_failed";
    public static final String INSERT_CATEGORY_EMPTY_TRANS= "category.create_category.create_empty_trans";
    public static final String CATEGORY_TRANSLATION_NAME_REQUIRED = "category.create_category.name_required";
    public static final String CATEGORY_TRANSLATION_LANGUAGE_REQUIRED= "category.create_category.language_required";
    public static final String UPDATE_CATEGORY_SUCCESSFULLY  = "category.update_category.update_successfully";
    public static final String UPDATE_CATEGORY_FAILED= "category.update_category.update_failed";
    public static final String CATEGORY_NOT_FOUND = "category.not_found";
    public static final String CATEGORY_NOT_FOUND_TRANSLATION = "category.not_found_translation";
    public static final String CATEGORY_RETRIEVED_SUCCESSFULLY = "category.retrieved_successfully";
    public static final String CATEGORY_RETRIEVED_FAILED = "category.retrieved_failed";

    //global
    public static final String VALIDATION_FAILED  = "validation.failed";
    public static final String REQUIRED_PART_MISSING= "required.part.missing";
    public static final String UNEXPECTED_ERROR= "unexpected.error";
    public static final String VALIDATION_IMAGE= "validation.image";
    public static final String VALIDATION_VIDEO= "validation.video";

    //auth
    public static final String INSUFFICIENT_AUTHENTICATION = "authentication.insufficient";
    public static final String INVALID_CREDENTIALS = "authentication.invalid_credentials";
    public static final String AUTHENTICATION_FAILED = "authentication.failed";
    public static final String ACCESS_DENIED = "authorization.access_denied";

    //language
    public static final String LANGUAGE_RETRIEVED_SUCCESSFULLY = "language.retrieved_successfully";
    public static final String LANGUAGE_RETRIEVED_FAILED = "language.retrieved_failed";

    //banner
    public static final String BANNER_RETRIEVED_SUCCESSFULLY = "banner.retrieved_successfully";
    public static final String BANNER_RETRIEVED_FAILED = "banner.retrieved_failed";
    public static final String INSERT_BANNER_SUCCESSFULLY = "banner.create_banner.create_successfully";
    public static final String INSERT_BANNER_FAILED = "banner.create_banner.create_failed";
    public static final String BANNER_DELETED_SUCCESSFULLY = "banner.delele_banner.deleted_successfully";
    public static final String BANNER_UPDATED_SUCCESSFULLY = "banner.update_banner.updated_successfully";

    //user
    public static final String PASSWORD_NOT_MATCH = "password.not_match";
    public static final String REGISTER_SUCCESSFULLY = "register.successfully";
    public static final String REGISTER_FAILED = "register.failed";
    public static final String LOGIN_SUCCESSFULLY = "login.successfully";
    public static final String LOGIN_FAILED = "login.failed";
    public static final String ADMIN_REGISTER_FORBIDDEN = "admin.register_forbidden";
    public static final String PHONE_ALREADY_EXISTS = "phone.already_exists";
    public static final String EMAIL_ALREADY_EXISTS = "email.already_exists";
    public static final String ROLE_DOES_NOT_EXISTS = "role.does_not_exist";
    public static final String WRONG_EMAIL_PASSWORD = "wrong.email_or_password";
    public static final String USER_IS_LOCKED = "user.is_locked";
    public static final String TOKEN_IS_EXPIRED = "token.is_expired";
    public static final String EMAIL_NOT_FOUND = "email.not_found";
    public static final String USER_NOT_FOUND = "user.not_found";
    public static final String FIRST_NAME_REQUIRED = "validation.first_name.required";
    public static final String LAST_NAME_REQUIRED = "validation.last_name.required";
    public static final String PHONE_REQUIRED = "validation.phone.required";
    public static final String PHONE_INVALID_FORMAT = "validation.phone.invalid_format";
    public static final String EMAIL_REQUIRED = "validation.email.required";
    public static final String EMAIL_INVALID_FORMAT = "validation.email.invalid_format";
    public static final String PASSWORD_REQUIRED = "validation.password.required";
    public static final String PASSWORD_INVALID_FORMAT = "validation.password.invalid_format";
    public static final String RETYPE_PASSWORD_REQUIRED = "validation.retype_password.required";
    public static final String ROLE_ID_REQUIRED = "validation.role_id.required";
    public static final String USER_DETAILS_RETRIEVED_SUCCESSFULLY = "user.details_retrieved_successfully";
    public static final String USER_DETAILS_RETRIEVED_FAILED = "user.details_retrieved_failed";
    public static final String USER_LIST_RETRIEVED_SUCCESSFULLY = "user.list_retrieved_successfully";
    public static final String USER_LIST_RETRIEVED_FAILED = "user.list_retrieved_failed";
    public static final String OTP_SENT_SUCCESSFULLY = "otp.sent_successfully";
    public static final String EMAIL_SEND_FAILED = "email.send_failed";
    public static final String OTP_VERIFIED_SUCCESSFULLY = "otp.verified_successfully";
    public static final String INVALID_OTP = "otp.invalid";
    public static final String OTP_INVALID_OR_EXPIRED = "otp.invalid_or_expired";
    public static final String OTP_VERIFICATION_FAILED = "otp.verification_failed";
    public static final String UPDATE_FAILED = "update.failed";
    public static final String UPDATE_SUCCESSFULLY = "update.successfully";
    public static final String RESET_PASSWORD_SUCCESSFULLY = "reset_password.successfully";
    public static final String INVALID_PASSWORD = "invalid.password";
    public static final String RESET_PASSWORD_FAILED = "reset_password.failed";
    public static final String ENABLE_SUCCESSFULLY = "enable.successfully";
    public static final String BLOCK_SUCCESSFULLY = "block.successfully";
    public static final String OTP_SEND_FAILED = "otp.send_failed";
    public static final String PASSWORD_CHANGED_SUCCESSFULLY = "password.changed_successfully";
    public static final String PASSWORD_CHANGE_FAILED = "password.change_failed";
    public static final String INCORRECT_CURRENT_PASSWORD = "password.incorrect_current";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "password.new_same_as_old";

    //store
    public static final String STORE_ID_REQUIRED_FOR_ROLE = "store.id_required_for_role";
    public static final String STORE_NOT_FOUND = "store.not_found";

    //staff
    public static final String STAFF_NOT_FOUND = "staff.not_found";

    //product
    public static final String PRODUCTS_RETRIEVED_SUCCESSFULLY = "products.retrieved_successfully";
    public static final String PRODUCTS_RETRIEVED_FAILED = "products.retrieved_failed";
    public static final String PRODUCT_VARIANTS_NOT_FOUND = "product.variants_not_found";
    public static final String PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY = "product.variants_retrieved_successfully";

    //product_media
    public static final String PRODUCT_MEDIA_NOT_FOUND = "product.media_not_found";


    //order
    public static final String ORDERS_SUCCESSFULLY = "orders.successfully";
    public static final String ORDERS_CREATE_FAILED = "orders.created_failed";



// cart
    public static final String CART_NOT_FOUND = "cart_not_found";

    public static final String  CART_VARIANTS_RETRIEVED_SUCCESSFULLY = "cart.variants_retrieved_successfully";

    // cartItem
    public static final String CART_ITEM_NOT_FOUND = "cartItem_by_id_not_found";


    // shipping_method
    public static final String SHIPPING_METHOD_NOT_VALID= "shipping_method_not_valid";


    // payment_method
    public static final String PAYMENT_METHOD_NOT_VALID = "payment_method_not_valid";

}
