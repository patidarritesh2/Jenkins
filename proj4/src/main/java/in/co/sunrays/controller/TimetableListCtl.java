package in.co.sunrays.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.sunrays.bean.BaseBean;
import in.co.sunrays.bean.CourseBean;
import in.co.sunrays.bean.SubjectBean;
import in.co.sunrays.bean.TimetableBean;
import in.co.sunrays.exception.ApplicationException;
import in.co.sunrays.model.CourseModel;
import in.co.sunrays.model.SubjectModel;
import in.co.sunrays.model.TimetableModel;
import in.co.sunrays.util.DataUtility;
import in.co.sunrays.util.PropertyReader;
import in.co.sunrays.util.ServletUtility;
/**
 *  Timetable functionality Controller. Performs operation for list, search
 * and delete operations of Timetable.
 * 
 * @author Rays 
 */
@WebServlet("/ctl/TimetableListCtl")
public class TimetableListCtl extends BaseCtl {
	
	private static Logger log = Logger.getLogger(TimetableListCtl.class);
	
	protected void preload(HttpServletRequest request) {
		CourseModel crsm = new CourseModel();
		SubjectModel stm = new SubjectModel();
		List<CourseBean> list = null;
		List<SubjectBean> list2 = null;
		try {
			list = crsm.list();
			list2 = stm.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		request.setAttribute("courseList", list);
		request.setAttribute("subjectList", list2);		
	}
	
	protected BaseBean populateBean(HttpServletRequest request) {
		TimetableBean tb = new TimetableBean();
		tb.setCourse_Id(DataUtility.getInt(request.getParameter("clist")));
		tb.setSubject_Id(DataUtility.getInt(request.getParameter("slist")));
		tb.setExam_Date(DataUtility.getDate(request.getParameter("Date")));
		populateDTO(tb, request);
		return tb;
	}
	
	// Contains Display Logics.......................
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		List list;
		int pageNo = 1;
		int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));
		TimetableModel tm = new TimetableModel();
		TimetableBean tb = (TimetableBean) populateBean(request);
		try {
			list = tm.search(tb, pageNo, pageSize);
			ServletUtility.setBean(tb, request);
			if (list == null && list.size() == 0) {
				ServletUtility.setErrorMessage("No record Found", request);
			}
			ServletUtility.setList(list, request);
			ServletUtility.setPageNo(pageNo, request);
			ServletUtility.setPageSize(pageSize, request);
			ServletUtility.forward(getView(), request, response);
		} catch (ApplicationException e) {
			e.printStackTrace();
			log.error(e);
			ServletUtility.handleException(e, request, response);
		}
	}
	
	// Contains Submit Logics..............................
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		List list;
		String op = DataUtility.getString(request.getParameter("operation"));
		
		int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
		int pageSize = DataUtility.getInt(request.getParameter("pageSize"));
		
		pageNo = (pageNo == 0) ? 1 : pageNo;
		pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

		TimetableBean bean = (TimetableBean) populateBean(request);
		TimetableModel model = new TimetableModel();
		String[] ids = (String[]) request.getParameterValues("ids");

		if (OP_SEARCH.equalsIgnoreCase(op)) {
			pageNo = 1;
		} else if (OP_NEXT.equalsIgnoreCase(op)) {
			pageNo++;
		} else if (OP_PREVIOUS.equalsIgnoreCase(op)) {
			if (pageNo < 1) {
				pageNo--;
			} else {
				pageNo = 1;
			}
		} else if (OP_NEW.equalsIgnoreCase(op)) {
			ServletUtility.redirect(ORSView.TIMETABLE_CTL, request, response);
			return;
		}

		else if (OP_RESET.equalsIgnoreCase(op)) {
			ServletUtility.redirect(ORSView.TIMETABLE_LIST_CTL, request, response);
			return;
		} else if (OP_DELETE.equalsIgnoreCase(op)) {
			pageNo = 1;
			if (ids != null && ids.length > 0) {
				TimetableBean bean3 = new TimetableBean();
				for (String id2 : ids) {
					int id1 = DataUtility.getInt(id2);
					bean3.setId(id1);
					try {
						model.delete(bean3);
					} catch (ApplicationException e) {
						e.printStackTrace();
						ServletUtility.handleException(e, request, response);
						return;
					}
					ServletUtility.setSuccessMessage("Timetable Data Deleted Succesfully", request);
				}
			} else {
				ServletUtility.setErrorMessage("Select at least one Record", request);
			}
		}
		try {
			list = model.search(bean, pageNo, pageSize);
			ServletUtility.setBean(bean, request);
		} catch (ApplicationException e) {
			ServletUtility.handleException(e, request, response);
			return;
		}
		if (list == null || list.size() == 0 && !OP_DELETE.equalsIgnoreCase(op)) {
			ServletUtility.setErrorMessage("No Record Found", request);
		}
		ServletUtility.setBean(bean, request);
		ServletUtility.setList(list, request);
		ServletUtility.setPageNo(pageNo, request);
		ServletUtility.setPageSize(pageSize, request);
		ServletUtility.forward(getView(), request, response);
	}

	@Override
	protected String getView() {
		// TODO Auto-generated method stub
		return ORSView.TIMETABLE_LIST_VIEW;
	}

}
