<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="../../../resources/css/goodsModify.css">

<script src="https://code.jquery.com/jquery-3.4.1.js"
	integrity="sha256-WpOohJOqMqqyKL9FccASB9O0KwACQJpFTUBLTYOVvVU="
	crossorigin="anonymous"></script>
<script
	src="https://cdn.ckeditor.com/ckeditor5/26.0.0/classic/ckeditor.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script src="//code.jquery.com/ui/1.8.18/jquery-ui.min.js"></script>
<style type="text/css">
#videobcg {
	position: absolute;
	top: 0px;
	left: 0px;
	min-width: 1920px;
	min-height: 1080px;
	width: 100%;
	height: 100%;
	z-index: -1000;
	overflow: hidden;
}
</style>
</head>

<style>
/* 깔끔하게 스크롤바싹없애는 */
html, body, div, video {
	margin: 0;
	padding: 0;
}

body {
	padding: 0px, 0px, 0px, 0px;
	margin: 0px, 0px, 0px, 0px;
	overflow: hidden;
	background-color: black;
}
</style>

<body>
	<video id="videobcg" preload="auto" autoplay="true" loop="loop"
		muted="muted">

		<source src="../../../resources/css/spaceGalaxy.mp4" type="video/mp4">

	</video>







	<div class="container">

		<div class="admin_content_main">
			<form action="/admin/goodsModify" method="post" id="modifyForm">
				<div class="neon_effect">

					



					<div class="inputText1">
						<label class="menuText">ship 제목</label>
					</div>
					<div class="inputbox">
						<input class="inputTag" name="shipName"
							value="${goodsInfo.shipName}">
					</div>







					<div class="inputText2">
						<label class="menuText">ship 가격</label>
					</div>
					<div class="inputbox">
						<input class="inputTag" name="shipPrice"
							value="${goodsInfo.shipPrice}">
					</div>





					<div class="inputText3">
						<label class="menuText">ship 재고</label>
					</div>
					<div class="inputbox">
						<input class="inputTag" name="shipStock"
							value="${goodsInfo.shipStock}">
					</div>





					<div class="inputText4">

						<label class="menuText">ship 소개</label>
					</div>
					<div class="areaBox">
						<textarea class="inputTagLarge" name="shipIntro"
							id="shipIntro_textarea">${goodsInfo.shipIntro}</textarea>
					</div>






				</div>

				<div class="form_section">
					<div class="form_section_content">
						<input type="file" id="fileItem" name='uploadFile' class="btn">
						<div id="uploadResult"></div>
					</div>
				</div>

				<input type="hidden" name='shipId' value="${goodsInfo.shipId}">
			</form>
		</div>




		<div class="btn_section">
			<button id="cancelBtn" class="btn">취 소</button>
			<button id="modifyBtn" class="btn ">수 정</button>
			<button id="deleteBtn" class="btn">삭 제</button>
		</div>
	</div>


	<form id="moveForm" action="/adminMenu/goodsManage" method="get">
		<input type="hidden" name="pageNum" value="${cri.pageNum}"> <input
			type="hidden" name="amount" value="${cri.amount}"> <input
			type="hidden" name="keyword" value="${cri.keyword}"> <input
			type="hidden" name='shipId' value="${goodsInfo.shipId}">
	</form>





	<script type="text/javascript">
		$(document)
				.ready(
						function() {
							/* 기존 이미지 출력 */
							let shipId = '<c:out value="${goodsInfo.shipId}"/>';
							let uploadResult = $("#uploadResult");

							$
									.getJSON(
											"/getAttachList",
											{
												shipId : shipId
											},
											function(arr) {

												console.log(arr);

												if (arr.length === 0) {

													let str = "";
													str += "<div id='result_card'>";
													str += "<img src='/resources/img/goodsNoImage.png'>";
													str += "</div>";

													uploadResult.html(str);
													return;
												}

												let str = "";
												let obj = arr[0];

												let fileCallPath = encodeURIComponent(obj.uploadPath
														+ "/s_"
														+ obj.uuid
														+ "_" + obj.fileName);
												str += "<div id='result_card'";
		str += "data-path='" + obj.uploadPath + "' data-uuid='" + obj.uuid + "' data-filename='" + obj.fileName + "'";
		str += ">";
												str += "<img src='/display?fileName="
														+ fileCallPath + "'>";
												str += "<div class='imgDeleteBtn' data-file='" + fileCallPath + "'>x</div>";
												str += "<input type='hidden' name='imageList[0].fileName' value='"+ obj.fileName +"'>";
												str += "<input type='hidden' name='imageList[0].uuid' value='"+ obj.uuid +"'>";
												str += "<input type='hidden' name='imageList[0].uploadPath' value='"+ obj.uploadPath +"'>";
												str += "</div>";

												uploadResult.html(str);

											});// GetJSON
						});

		/* 취소 버튼 */
		$("#cancelBtn").on("click", function(e) {
			e.preventDefault();
			$("#moveForm").submit();
		});

		/* 삭제 버튼 */
		$("#deleteBtn")
				.on(
						"click",
						function(e) {
							e.preventDefault();
							let moveForm = $("#moveForm");
							moveForm.find("input").remove();
							moveForm
									.append('<input type="hidden" name="shipId" value="${goodsInfo.shipId}">');
							moveForm.attr("action", "/goodsDelete");
							moveForm.attr("method", "post");
							moveForm.submit();
						});

		/* 수정 버튼 */
		$("#modifyBtn").on("click", function(e) {
			e.preventDefault();
			$("#modifyForm").submit();
		});

		/* 이미지 삭제 버튼 동작 */
		$("#uploadResult").on("click", ".imgDeleteBtn", function(e) {

			deleteFile();

		});

		/* 파일 삭제 메서드 */
		function deleteFile() {
			$("#result_card").remove();
		}

		/* 이미지 업로드 */
		$("input[type='file']").on("change", function(e) {

			/* 이미지 존재시 삭제 */
			if ($("#result_card").length > 0) {
				deleteFile();
			}

			let formData = new FormData();
			let fileInput = $('input[name="uploadFile"]');
			let fileList = fileInput[0].files;
			let fileObj = fileList[0];

			if (!fileCheck(fileObj.name, fileObj.size)) {
				return false;
			}

			formData.append("uploadFile", fileObj);

			$.ajax({
				url : '/uploadAjaxAction',
				processData : false,
				contentType : false,
				data : formData,
				type : 'POST',
				dataType : 'json',
				success : function(result) {
					console.log(result);
					showUploadImage(result);
				},
				error : function(result) {
					alert("이미지 파일이 아닙니다.");
				}
			});

		});

		/* var, method related with attachFile */
		let regex = new RegExp("(.*?)\.(jpg|png)$");
		let maxSize = 104857600; //1MB	

		function fileCheck(fileName, fileSize) {

			if (fileSize >= maxSize) {
				alert("파일 사이즈 초과");
				return false;
			}

			if (!regex.test(fileName)) {
				alert("해당 종류의 파일은 업로드할 수 없습니다.");
				return false;
			}

			return true;

		}

		/* 이미지 출력 */
		function showUploadImage(uploadResultArr) {

			/* 전달받은 데이터 검증 */
			if (!uploadResultArr || uploadResultArr.length == 0) {
				return
			}

			let uploadResult = $("#uploadResult");

			let obj = uploadResultArr[0];

			let str = "";

			let fileCallPath = encodeURIComponent(obj.uploadPath.replace(/\\/g,
					'/')
					+ "/s_" + obj.uuid + "_" + obj.fileName);
			//replace 적용 하지 않아도 가능
			//let fileCallPath = encodeURIComponent(obj.uploadPath + "/s_" + obj.uuid + "_" + obj.fileName);

			str += "<div id='result_card'>";
			str += "<img src='/display?fileName=" + fileCallPath + "'>";
			str += "<div class='imgDeleteBtn' data-file='" + fileCallPath + "'>x</div>";
			str += "<input type='hidden' name='imageList[0].fileName' value='"+ obj.fileName +"'>";
			str += "<input type='hidden' name='imageList[0].uuid' value='"+ obj.uuid +"'>";
			str += "<input type='hidden' name='imageList[0].uploadPath' value='"+ obj.uploadPath +"'>";
			str += "</div>";

			uploadResult.append(str);

		}
	</script>
</body>
</html>