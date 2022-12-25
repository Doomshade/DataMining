package cz.zcu.jsmahy.datamining


import javafx.stage.Stage
import org.testfx.api.FxAssert
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec
import org.testfx.matcher.control.LabeledMatchers
import org.testfx.util.WaitForAsyncUtils

/**
 * @author Jakub Šmrha
 * @version $VERSION
 * @since 25.12.2022
 */
class ApplicationGUISpec extends ApplicationSpec {

    @Override
    void init() throws Exception {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication(Main.class);
        FxToolkit.showStage()
        WaitForAsyncUtils.waitForFxEvents(100);
    }

    @Override
    void start(final Stage stage) throws Exception {
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    def "should click on button"() {
        given:
        def button = lookup(".button").queryButton()

        when:
        clickOn(button)

        then:
        FxAssert.verifyThat(button, LabeledMatchers.hasText("Hledat"))
    }
}
