package uk.ac.ebi.thor.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;

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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoginOrcId.
 * @author Guilherme Formaggio
 */
@WebServlet(DataClaimingService.URL_CALLBACKLOGIN)
public class LoginCallbackServlet extends AbstractAuthorizationCodeCallbackServlet {

  private static final long serialVersionUID = -4181134403039504075L;

  @Autowired
  private transient DataClaimingService service;

  @Override
  protected void
      onError(HttpServletRequest req, 
              HttpServletResponse resp, 
              AuthorizationCodeResponseUrl errorResponse)
          throws ServletException, IOException {
    throw new ServletException("An error occurred during login!");
  }

  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    //Register success of user login using OAuth
    service.registerOrcIdDetails(req, credential);
    service.registerCookie(req, resp);

    //At user side, refresh the claiming informations and close the popup
    String nextJsp = "/popup.jsp";
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJsp);
    dispatcher.forward(req, resp);
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response) .
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req)
      throws ServletException, IOException {
    return service.getCallBackUrl(req);
  }

  @Override
  protected String getUserId(HttpServletRequest req)
      throws ServletException, IOException {
    return service.retrieveUserSession(req).getSessionId();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow()
      throws ServletException, IOException {
    return service.createAuthorizationFlow(true);
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
}
