package com.example.jshop.error_handlers;

import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.error_handlers.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalHttpErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception) {
        return new ResponseEntity<>("Access denied", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CategoryExistsException.class)
    public ResponseEntity<Object> handleCategoryExistsException(CategoryExistsException exception) {
        return new ResponseEntity<>("Category already exists", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCategoryNameException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(InvalidCategoryNameException exception) {
        return new ResponseEntity<>("Provide proper name", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Object> handleCategoryNotFoundException(CategoryNotFoundException exception) {
        return new ResponseEntity<>("Category not found", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Object> handleProductAlreadyExistsException(ProductAlreadyExistsException exception) {
        return new ResponseEntity<>("Product exists, update product instead", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Object> handleCartNotFoundException(CartNotFoundException exception) {
        return new ResponseEntity<>("Cart with given ID Not Found", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ItemNotAvailableException.class)
    public ResponseEntity<Object> handleItemNotAvailableException(ItemNotAvailableException exception) {
        return new ResponseEntity<>("Currently item is not available", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotEnoughItemsException.class)
    public ResponseEntity<Object> handleNotEnoughItemsException(NotEnoughItemsException exception) {
        return new ResponseEntity<>("The quantity of selected items is currently not available", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFoundException(ProductNotFoundException exception) {
        return new ResponseEntity<>("Product with given Id not found", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<Object> handleCategoryException(CategoryException exception) {
        return new ResponseEntity<>("Deleting category \"Unknown\" denied", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<Object> handleInvalidQuantityException(InvalidQuantityException exception) {
        return new ResponseEntity<>("Provided quantity is out of range 1 - 2 147 483 647", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException exception) {
        return new ResponseEntity<>("User does not exist", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFoundException(OrderNotFoundException exception) {
        return new ResponseEntity<>("Order does not exist", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentErrorException.class)
    public ResponseEntity<Object> handlePaymentErrorException(PaymentErrorException exception) {
        return new ResponseEntity<>("Failure with payment", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleSQLException(SQLException exception) {
        return new ResponseEntity<>("SQLException", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<Object> handleInvalidPriceException(InvalidPriceException exception) {
        return new ResponseEntity<>("Price is incorrect", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LimitException.class)
    public ResponseEntity<Object> handleLimitException(LimitException exception) {
        return new ResponseEntity<>("Limit range should be between 1 and 100", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<Object> handleLimitException(InvalidOrderStatusException exception) {
        return new ResponseEntity<>("Provide proper status. Status can be \"paid\" or \"unpaid\"", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCustomerDataException.class)
    public ResponseEntity<Object> handleInvalidCustomerDataException(InvalidCustomerDataException exception) {
        return new ResponseEntity<>("Fields cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
}

