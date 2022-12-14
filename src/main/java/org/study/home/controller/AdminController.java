package org.study.home.controller;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.study.home.mapper.AttachMapper;
import org.study.home.mapper.MemberMapper;
import org.study.home.model.AttachImageDTO;
import org.study.home.model.Criteria;
import org.study.home.model.MemberDTO;
import org.study.home.model.PageDTO;
import org.study.home.model.ShipDTO;
import org.study.home.service.AdminService;
import org.study.home.service.MemberService;

import net.coobird.thumbnailator.Thumbnails;

@Controller
public class AdminController {
	@Autowired
	private MemberMapper mapper;

	@Autowired
	private MemberService memberService;
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private AdminService adminService;

	@Autowired
	private AttachMapper attachMapper;

	@GetMapping("/adminMenu/adminMember")
	public String adminMember(Model model) {

		List<MemberDTO> list = memberService.userList();
		model.addAttribute("list", list);

		return "board/adminMember";
	}

	@GetMapping("/adminMenu")
	public String adminMenu() {
		return "board/adminMenu";
	}

	@GetMapping("/adminMenu/goodsEnroll")
	public String goodsEnroll() {
		return "admin/goodsEnroll";
	}
	
	
	
	 @GetMapping("/goodsRead")
	   public String goodsRead(@RequestParam("shipId")String shipId, Model model) {
	    ShipDTO dto= adminService.goodsRead(shipId);
	    model.addAttribute("dto",dto);
	      return "adminMenu/goodsRead";
	   }

	   @GetMapping("/goodsUpdate")
	   public String goodsUpdate(@RequestParam("shipId")String shipId, Model model) {
		   ShipDTO dto=adminService.goodsRead(shipId);
	       model.addAttribute("dto",dto);
	      return "adminMenu/goodsUpdate";
	   }

	   @PostMapping("/goodsUpdate")
	   public String goodsUpdateProcess(ShipDTO dto) {
	      adminService.goodsUpdate(dto);
	      System.out.println("update ----" + dto.toString());

	  return "redirect:/goodsRead?shipId="+dto.getShipId();
	   }

	
	
	
	
	
	
	
	

	@RequestMapping(value = "/adminMenu/goodsManage", method = RequestMethod.GET)
	public void goodsManage(Criteria cri, Model model) throws Exception {
		/* ?????? ????????? ????????? */

		cri.setSkip((cri.getPageNum() - 1) * 10);
		List<ShipDTO> list = adminService.goodsGetList(cri);

		if (!list.isEmpty()) {
			model.addAttribute("list", list);
		} else {
			model.addAttribute("listCheck", "empty");
			return;
		}

		/* ????????? ??????????????? ????????? */
		model.addAttribute("pageMaker", new PageDTO(cri, adminService.goodsGetTotal(cri)));

	}

	/* ?????? ?????? */
	@PostMapping("/adminMenu/goodsEnroll")
	public String goodsEnrollPOST(ShipDTO ship, RedirectAttributes rttr) {

		logger.info("goodsEnrollPOST......" + ship);

		adminService.shipEnroll(ship);

		rttr.addFlashAttribute("enroll_result", ship.getShipName());

		return "redirect:/adminMenu/goodsManage";
	}

	/* ?????? ?????? ????????? */
	@PostMapping(value = "/uploadAjaxAction", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<AttachImageDTO>> uploadAjaxActionPOST(MultipartFile[] uploadFile) {

		/* ????????? ?????? ?????? */
		for (MultipartFile multipartFile : uploadFile) {

			File checkfile = new File(multipartFile.getOriginalFilename());
			String type = null;

			try {
				type = Files.probeContentType(checkfile.toPath());
				logger.info("MIME TYPE : " + type);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!type.startsWith("image")) {
				List<AttachImageDTO> list = null;
				return new ResponseEntity<>(list, HttpStatus.BAD_REQUEST);
			}
		}
		String uploadFolder = "/home/lwi/image/";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String str = sdf.format(date);
		String datePath = str.replace("-", File.separator);

		/* ?????? ?????? */
		File uploadPath = new File(uploadFolder, datePath);

		if (uploadPath.exists() == false) {
			uploadPath.mkdirs();
		}

		/* ????????? ?????? ?????? ?????? */
		List<AttachImageDTO> list = new ArrayList<AttachImageDTO>();
		// ????????? for
		for (MultipartFile multipartFile : uploadFile) {

			AttachImageDTO dto = new AttachImageDTO();
			/* ?????? ?????? */
			String uploadFileName = multipartFile.getOriginalFilename();
			dto.setFileName(uploadFileName);
			dto.setUploadPath(datePath);
			/* uuid ?????? ?????? ?????? */
			String uuid = UUID.randomUUID().toString();
			dto.setUuid(uuid);

			uploadFileName = uuid + "_" + uploadFileName;

			/* ?????? ??????, ?????? ????????? ?????? File ?????? */
			File saveFile = new File(uploadPath, uploadFileName);

			/* ?????? ?????? */
			try {
				multipartFile.transferTo(saveFile);

				/* ?????? 2 */
				File thumbnailFile = new File(uploadPath, "s_" + uploadFileName);

				BufferedImage bo_image = ImageIO.read(saveFile);

				// ??????
				double ratio = 3;
				// ?????? ??????
				int width = (int) (bo_image.getWidth() / ratio);
				int height = (int) (bo_image.getHeight() / ratio);

				Thumbnails.of(saveFile).size(width, height).toFile(thumbnailFile);

			} catch (Exception e) {
				e.printStackTrace();
			}
			list.add(dto);
		}

		ResponseEntity<List<AttachImageDTO>> result = new ResponseEntity<List<AttachImageDTO>>(list, HttpStatus.OK);
		return result;

	}

	@GetMapping("/display")
	public ResponseEntity<byte[]> getImage(String fileName) {
		logger.info("getImage()......." + fileName);

		File file = new File("/home/lwi/image/" + fileName);
		ResponseEntity<byte[]> result = null;

		try {

			HttpHeaders header = new HttpHeaders();

			header.add("Content-type", Files.probeContentType(file.toPath()));

			result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/* ????????? ?????? ?????? */
	@PostMapping("/deleteFile")
	public ResponseEntity<String> deleteFile(String fileName) {

		logger.info("deleteFile........" + fileName);
		File file = null;
		try {
			/* ????????? ?????? ?????? */
			file = new File("/home/lwi/image/" + URLDecoder.decode(fileName, "UTF-8"));

			file.delete();

			/* ?????? ?????? ?????? */
			String originFileName = file.getAbsolutePath().replace("s_", "");

			logger.info("originFileName : " + originFileName);

			file = new File(originFileName);

			file.delete();

		} catch (Exception e) {

			e.printStackTrace();

			return new ResponseEntity<String>("fail", HttpStatus.NOT_IMPLEMENTED);

		}
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	/* ????????? ?????? ?????? */
	@SuppressWarnings("deprecation")
	@GetMapping(value="/getAttachList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<AttachImageDTO>> getAttachList(int shipId){
		
		logger.info("getAttachList.........." + shipId);
		
		return new ResponseEntity<List<AttachImageDTO>>(attachMapper.getAttachList(shipId), HttpStatus.OK);
		
	}
	
	
	/* ?????? ?????? ????????? */
	@GetMapping({"/admin/goodsDetail","/admin/goodsModify"})
	public void goodsGetInfoGET(int shipId, Criteria cri, Model model) {
		
		logger.info("goodsGetInfo()........." + shipId);
		
		/* ?????? ????????? ?????? ?????? */
		model.addAttribute("cri", cri);
		
		/* ?????? ????????? ?????? */
		model.addAttribute("goodsInfo", adminService.goodsGetDetail(shipId));

	}
	
	
	/* ?????? ?????? ?????? */
	@PostMapping("/admin/goodsModify")
	public String goodsModifyPOST(ShipDTO dto, RedirectAttributes rttr) {
		
		logger.info("goodsModifyPOST.........." + dto);
		
		int result = adminService.goodsModify(dto);
		
		rttr.addFlashAttribute("modify_result", result);
		
		return "redirect:/adminMenu/goodsManage";		
		
	}
	
	/* ?????? ?????? ?????? */
	@PostMapping("/goodsDelete")
	public String goodsDeletePOST(int shipId, RedirectAttributes rttr) {
		
		logger.info("goodsDeletePOST..........");
		
		int result = adminService.goodsDelete(shipId);
		
		rttr.addFlashAttribute("delete_result", result);
		
		return "redirect:/adminMenu/goodsManage";
		
	}
	

}