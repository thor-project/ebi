package uk.ac.ebi.thor.filter;

import uk.ac.ebi.thor.service.DataClaimingService;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * Servlet Filter implementation class SimpleCORSFilter.
 */
@WebFilter("/*")
public class SimpleCorsFilter implements Filter {

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain).
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) res;
    //All THOR clients must be registered in this string to enable javascript integration
    response.addHeader("Access-Control-Allow-Origin", getClientAddress((HttpServletRequest) req));
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Headers",
        "Origin, X-Requested-With, Content-Type, Accept");
    chain.doFilter(req, res);
  }

  /**
   * Gets the user server name to allow control origin for javascript communication among distinct
   * servers.
   * @param req
   *        ServletRequest
   * @return String
   */
  private String getClientAddress(HttpServletRequest req) {
    HttpSession session = req.getSession();
    String clientAddress = req.getHeader("origin");
    if (clientAddress != null) {
      session.setAttribute(DataClaimingService.PARAM_CLIENTADD, clientAddress);
    } else {
      clientAddress = (String) session.getAttribute(DataClaimingService.PARAM_CLIENTADD);
    }
    return clientAddress;
  }

  /**
   * @see Filter#destroy().
   */
  @Override
  public void destroy() {
    //Implementation of abstract method.
  }

  /**
   * @see Filter#init(FilterConfig).
   */
  @Override
  public void init(FilterConfig filterConfig)
      throws ServletException {
    //Implementation of abstract method.
  }

}
