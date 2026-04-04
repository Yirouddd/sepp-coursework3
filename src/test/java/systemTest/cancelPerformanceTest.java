package systemTest;

import external.PaymentSystem;
import interfaces.View;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class cancelPerformanceTest {

    private static View mockView;

    @BeforeAll
    static void setUpAll() {
        mockView = mock(View.class);
    }




}
