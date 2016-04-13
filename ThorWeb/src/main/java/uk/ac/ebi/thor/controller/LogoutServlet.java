package uk.ac.ebi.thor.controller;

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
 * Servlet implementation class LogoutServlet .
 */
@WebServlet(DataClaimingService.URL_LOGOUT)
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  @Autowired
  private transient DataClaimingService service;
  
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) .
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    service.registerUserLogout(request, response);

    //At user side, refresh the claiming informations and close the popup
    String nextJsp = "/popup.jsp";
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJsp);
    dispatcher.forward(request, response);
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
  public void init(final ServletConfig config)
      throws ServletException {
    super.init(config);
    WebApplicationContext springContext = WebApplicationContextUtils
        .getRequiredWebApplicationContext(config.getServletContext());
    final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
    beanFactory.autowireBean(this);
  }

}
