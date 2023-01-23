package cz.zcu.jsmahy.datamining

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage
import org.testfx.api.FxAssert
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec
import org.testfx.matcher.control.LabeledMatchers
import org.testfx.util.WaitForAsyncUtils
import spock.lang.Ignore

import static javafx.scene.input.KeyCombination.ModifierValue.ANY
import static javafx.scene.input.KeyCombination.ModifierValue.DOWN

/**
 * @author Jakub Šmrha
 * @version $VERSION
 * @since 25.12.2022
 */
class ApplicationGUISpec extends ApplicationSpec {
    private ResourceBundle resourceBundle

    void setup() {
        resourceBundle = ResourceBundle.getBundle("lang")
    }

    @Override
    void init() throws Exception {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication(Main.class)
        FxToolkit.showStage()
        WaitForAsyncUtils.waitForFxEvents(100)
    }

    @Override
    void start(final Stage stage) throws Exception {
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    @Ignore
    def "should click on button"() {
        given:
        def button = lookup("#searchButton").queryButton()

        when:
        clickOn(button)

        then:
        FxAssert.verifyThat(button, LabeledMatchers.hasText(resourceBundle.getString("search")))
    }

    @Ignore
    def "Test search for debug"() {
        given:
        push(new KeyCodeCombination(KeyCode.N, ANY, DOWN, ANY, ANY, ANY))
        type(KeyCode.A, KeyCode.ENTER)
        push(new KeyCodeCombination(KeyCode.H, ANY, ANY, DOWN, ANY, ANY))
        push(new KeyCodeCombination(KeyCode.A, DOWN, ANY, ANY, ANY, ANY))
        type(KeyCode.L, KeyCode.B, KeyCode.E, KeyCode.R, KeyCode.T, KeyCode.SPACE)
        push(new KeyCodeCombination(KeyCode.E, DOWN, ANY, ANY, ANY, ANY))
        type(KeyCode.I, KeyCode.N, KeyCode.S, KeyCode.T, KeyCode.E, KeyCode.I, KeyCode.N, KeyCode.ENTER)
        synchronized (this) {
            wait(5000)
        }
    }
}
