package com.memoria.Memoria.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoteNotFoundException.class)
    public String handleNoteNotFound(NoteNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/notes";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorized(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to access that note.");
        return "redirect:/notes";
    }
}
