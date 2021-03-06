package ru.xpoft.vaadin;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author xpoft
 */
public class SpringUIProvider extends UIProvider
{
    private static Logger logger = LoggerFactory.getLogger(SpringUIProvider.class);

    /**
     * Servlet parameter name for UI bean
     */
    protected static final String BEAN_NAME_PARAMETER = "beanName";

    public SpringUIProvider()
    {
    }

    @Override
    public UI createInstance(UICreateEvent event)
    {
        return (UI) SpringApplicationContext.getApplicationContext().getBean(getUIBeanName(event.getRequest()));
    }

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event)
    {
        // Add session-scoped UI to current window
        if (this.isSessionScopedUI(event.getRequest()))
        {
            String windowName = event.getRequest().getParameter("v-wn");

            Map<String, Integer> retainOnRefreshUIs = VaadinSession.getCurrent().getPreserveOnRefreshUIs();
            if (windowName != null && !retainOnRefreshUIs.isEmpty() && retainOnRefreshUIs.get(windowName) == null)
            {
                // Check for session-scope-UI
                for(Map.Entry<String, Integer> entry : retainOnRefreshUIs.entrySet())
                {
                    UI ui = VaadinSession.getCurrent().getUIById(entry.getValue());
                    VaadinSession.getCurrent().getPreserveOnRefreshUIs().put(windowName, ui.getUIId());
                    break;
                }
            }
        }

        return (Class<? extends UI>) SpringApplicationContext.getApplicationContext().getType(getUIBeanName(event.getRequest()));
    }

    @Override
    public boolean isPreservedOnRefresh(UICreateEvent event)
    {
        if (isSessionScopedUI(event.getRequest()))
        {
            return true;
        }

        return super.isPreservedOnRefresh(event);
    }

    public boolean isSessionScopedUI(VaadinRequest request)
    {
        return !SpringApplicationContext.getApplicationContext().isPrototype(getUIBeanName(request));
    }

    /**
     * Returns the bean name to be retrieved from the application bean context and
     * used as the UI. The default implementation uses the servlet init property
     * {@link #BEAN_NAME_PARAMETER} or "ui" if not defined.
     *
     * @param request the current Vaadin request
     * @return the UI bean name in the application context
     */
    protected String getUIBeanName(VaadinRequest request)
    {
        String vaadinBeanName = "ui";

        Object uiBeanName = request.getService().getDeploymentConfiguration().getApplicationOrSystemProperty(BEAN_NAME_PARAMETER, null);
        if (uiBeanName != null && uiBeanName instanceof String)
        {
            vaadinBeanName = uiBeanName.toString();
        }

        logger.debug("found BEAN_NAME_PARAMETER: {}", vaadinBeanName);
        return vaadinBeanName;
    }
}
