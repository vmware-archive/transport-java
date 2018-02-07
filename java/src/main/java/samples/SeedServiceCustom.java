package samples;

import org.springframework.stereotype.Component;

@CustomServiceCode(
        serviceName="pizza"
)
@Component
public class Testy {

    @CustomServiceCodeHandler(stage = RunStage.Before, methodName = "handleSomething")
    public void handleSomething() {

    }

}
