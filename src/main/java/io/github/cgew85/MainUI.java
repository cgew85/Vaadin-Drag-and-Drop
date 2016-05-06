package io.github.cgew85;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Locale;

/**
 * Created by cgew85 on 06.05.2016.
 */

@SpringUI
@Theme("valo")
@Title("PoC Vaadin Drag and Drop")
public class MainUI extends UI {

    @Autowired
    private SpringViewProvider springViewProvider;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setLocale(Locale.GERMANY);

        final Navigator navigator = new Navigator(this, this);
        navigator.addProvider(springViewProvider);
        navigator.navigateTo(MainView.VIEW_NAME);
    }
}
