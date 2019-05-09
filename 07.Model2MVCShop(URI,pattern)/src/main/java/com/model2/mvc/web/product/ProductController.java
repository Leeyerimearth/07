package com.model2.mvc.web.product;

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;

@Controller
@RequestMapping("/product/*")
public class ProductController {

	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	
	@Value("#{commonProperties['pageUnit']}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	int pageSize;
	
	public ProductController() {
		System.out.println(this.getClass().getName()+"컨트롤러 생성자");
	}

	@RequestMapping(value="addProduct", method=RequestMethod.POST) // 항상 post로만 등록가능
	public String addProduct(@ModelAttribute("product") Product product) throws Exception
	{
		System.out.println("/product/addProduct");
		
		productService.addProduct(product);
		
		return "forward:/product/addProductResultView.jsp";
	}
	
	@RequestMapping(value="getProduct", method=RequestMethod.GET)
	public String getProduct(@RequestParam("prodNo") String prodNo,HttpSession session,HttpServletRequest request,
									HttpServletResponse response) throws Exception
	{
		System.out.println("/product/getProduct");
		
		Product vo = productService.getProduct(Integer.parseInt(prodNo));

		String cookieString =this.addCookie(prodNo, request, response); // addCookie method화
		
		System.out.println(cookieString);
		
		session.setAttribute("vo", vo);
		String menu = (String) session.getAttribute("menu");
		System.out.println(menu);
		
		if(menu.equals("manage"))
			return "forward:/product/updateProduct"; // 이렇게 들어오면 ----
		else
			return "forward:/product/readProduct.jsp";
		
	}
	
	@RequestMapping(value="updateProduct", method=RequestMethod.GET) // 위에서 getProduct가 get방식. forward하면 유지
	public String updateProduct()
	{
		System.out.println("/product/updateProductView 실행");
		
		return "forward:/product/updateProductView.jsp";
	}
	
	@RequestMapping(value="updateProduct",method=RequestMethod.POST)
	public String updateProduct(@ModelAttribute("product") Product product, HttpSession session, Model model) throws Exception
	{
		System.out.println("/product/updateProduct 실행 했다리!");
		
		Product sessionProduct = (Product) session.getAttribute("vo");
		
		product.setProdNo(sessionProduct.getProdNo());
		product.setRegDate(sessionProduct.getRegDate());
		
		productService.updateProduct(product);
		
		model.addAttribute("vo", product);
		
		return "forward:/product/readProduct2.jsp";
	}
	
	@RequestMapping("listProduct") //기존 listProduct trancode때문인것같은데 필요없댜
	public String listProduct(@ModelAttribute("search") Search search,Model model,HttpSession session,
									@RequestParam("menu") String menu) throws Exception
	{
		System.out.println("/product/listProduct");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String,Object> map = productService.getProductList2(search);
		Page resultPage	= 
				new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		
		session.setAttribute("menu", menu);
		
		return "forward:/product/listProduct.jsp";
	}
	
	@RequestMapping("listProduct2") //수정 listProduct
	public String listProduct2(@ModelAttribute("search") Search search,Model model,HttpSession session,
			@RequestParam("menu") String menu) throws Exception
	{
			System.out.println("/product/listProduct2");

			if(search.getCurrentPage() ==0 ){
				search.setCurrentPage(1);
			}
			search.setPageSize(pageSize);

			Map<String,Object> map = productService.getProductList(search);
			Page resultPage	= 
					new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);

			model.addAttribute("list", map.get("list"));
			model.addAttribute("resultPage", resultPage);
			model.addAttribute("search", search);

			session.setAttribute("menu", menu);

			return "forward:/product/listProduct2.jsp"; //판매상품관리로 forward
	}
	
	@RequestMapping("bestSellerList")
	public String bestSellerList(Model model)
	{
		
		System.out.println("/product/bestSellerList");
		
		// search 필요없다.페이지는 무조건 1개로 놓을거기 때문에.
		
		List<Product> list = productService.getBestSellerList();
		
		model.addAttribute("list", list);
		
		return "forward:/product/bestSellerList.jsp";
	}
	
	
	private String addCookie(String prodNo,HttpServletRequest request,HttpServletResponse response)
	{
		String cookieString = "";
		int count=0;
		
		if(request.getCookies()!=null) // cookie가 널이 아닐때 하면안된다. cookie는 null이 아니다.
		{
			Cookie[] cookieJar = request.getCookies();
			for(int i=0 ; i<cookieJar.length; i++)
			{
				Cookie cookie = cookieJar[i];
				if(cookie.getName().equals("history")) // history cookie가 있을때,
					cookieString = cookie.getValue()+","+prodNo;	
				else // cookie는 있지만, history cookie가 없을때.
 					count++;
			}
			if(count==cookieJar.length)
				cookieString =prodNo;
			
		}
		else // history는 물론 아예 쿠키가 0일때.
		{
			cookieString = prodNo; // cookieString에다가 첫 prodNo를 더한다.
		}
		Cookie cookie = new Cookie("history",cookieString);
		cookie.setMaxAge(-1);
		cookie.setPath("/"); //전체 경로에 저장.
		response.addCookie(cookie);
		
		return cookieString;
	}
	
	
}
