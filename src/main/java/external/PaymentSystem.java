package external;


/**
 * Interface for the external {@link PaymentSystem}. It allows requesting for payments to be made by a student when
 * booking, or for payments to be refunded. After the payment system is called, the remainder of the payment process
 * is considered external to our system (but our system does get a confirmation of a success or failure).
 * There is only one {@link PaymentSystem} and all users of this application use the same  system. Payments and
 * refunds can succeed or fail, this is indicated by the return values.
 */
public interface PaymentSystem {
    /**
     * Request a payment to be made from a student when booking. Most of this information is necessary so the payment
     * system can notify the university admin of the details of the transaction.
     *
     * @param numTickets        Amount of tickets bought.
     * @param eventTitle        The title of the event the student is booking.
     * @param studentEmail      The email address of the student making the payment.
     * @param studentPhone      The phone number of the student making the payment.
     * @param epEmail           The email address Entertainment Provider that is providing the booked event.
     * @param transactionAmount Amount to be transferred in GBP.
     * @return True if successful and false otherwise
     */
    boolean processPayment(int numTickets, String eventTitle, String studentEmail, int studentPhone,
                           String epEmail, double transactionAmount);

    /**
     * Request  payment to be refunded from the EP to the student for a given transaction amount. Most of this
     * information is necessary so the payment system can notify the university admin of the details of the transaction.
     *
     * @param numTickets        Amount of tickets that were originally bought.
     * @param eventTitle        The title of the event the student had booked.
     * @param studentEmail      The email address of the student that made the payment.
     * @param studentPhone      The phone number of the student that made the payment.
     * @param epEmail           The email address Entertainment Provider that provided the booked event.
     * @param transactionAmount Original amount paid for the booking that needs to be transferred, in GBP.
     * @param organiserMsg      If an event is cancelled by the EP, this message from the EP will be sent to affected
     *                          students. Otherwise it's empty.
     * @return True if successful and false otherwise
     */
    boolean processRefund(int numTickets, String eventTitle, String studentEmail, int studentPhone,
                          String epEmail, double transactionAmount, String organiserMsg);
}