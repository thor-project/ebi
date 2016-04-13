package uk.ac.ebi.thor.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.ac.ebi.thor.service.DataClaimingService;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation of AbstractAuthorizationCodeServlet
 * Responsible for the user login thru OrcId OAuth.
 * 
 * @author Guilherme Formaggio
 *
 */
@WebServlet(DataClaimingService.URL_LOGIN)
public class LoginServlet extends AbstractAuthorizationCodeServlet {

  private static final long serialVersionUID = 8833967981024864211L;
  
  @Autowired
  private transient DataClaimingService service;

  @Override
  protected String getRedirectUri(HttpServletRequest req)
      throws ServletException, IOException {
    return service.getCallBackUrl(req);
  }

  @Override
  protected String getUserId(HttpServletRequest request)
      throws ServletException, IOException {
    return service.retrieveUserSession(request).getSessionId();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow()
      throws ServletException, IOException {
    return service.createAuthorizationFlow(false);
  }

  @Override
  public void init(final ServletConfig config)
      throws ServletException {
    super.init(config);
    WebApplicationContext springContext = WebApplicationContextUtils
        .getRequiredWebApplicationContext(config.getServletContext());
    final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
    beanFactory.autowireBean(this);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    service.registerRememberMe(req);
    service.registerClientAddress(req);
    super.service(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    //At user side, refresh the claiming informations and close the popup
    String nextJsp = "/popup.jsp";
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJsp);
    dispatcher.forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doGet(req, resp);
  }
  
  

}
