package com.thekielcebarber.barbershop.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.thekielcebarber.barbershop.model.Appointment;
import com.thekielcebarber.barbershop.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Controller
public class StripeController {

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/payment/create-checkout-session")
    @ResponseBody
    public Map<String, String> createCheckoutSession(@RequestParam Long appointmentId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 1. Buscamos la cita real en la base de datos
            Appointment appt = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

            // 2. Calculamos el precio en céntimos (Stripe usa céntimos)
            // Ejemplo: 15.00€ -> 1500 céntimos
            long priceInCents = (long) (appt.getPrice() * 100);

            // 3. Configuramos la sesión con datos dinámicos
            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // Redirige al éxito donde el AppointmentController marcará como PAID_ONLINE
                .setSuccessUrl("http://localhost:8080/appointments/payment-success?id=" + appointmentId)
                .setCancelUrl("http://localhost:8080/appointments/my-appointments?payment=cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("eur")
                        .setUnitAmount(priceInCents) // <--- PRECIO DINÁMICO
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(appt.getService().getName() + " - The Kielce Barber") // Nombre real del servicio
                            .build())
                        .build())
                    .build())
                .build();

            Session session = Session.create(params);
            response.put("id", session.getId());
            return response;
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return response;
        }
    }
}