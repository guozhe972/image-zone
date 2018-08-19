$(function() {
	//$('body').append(
	//		'<button type="button" id="toTop" class="btn btn-primary rounded-circle">'
	//				+ '<i class="fas fa-angle-up fa-2x"></i>' + '</button>');
	$(window).scroll(function() {
		if ($(this).scrollTop() > 100) {
			$('#toTop').fadeIn();
		} else {
			$('#toTop').fadeOut();
		}
	});
	$('#toTop').click(function() {
		$('html, body').animate({
			scrollTop : 0
		}, 300);
		return false;
	});

	$('input.num-only').on("input", function() {
		this.value = this.value.replace(/\D/g, '');
		this.value = this.value.slice(0, this.maxLength);
	});
});

function showAlertError(arrMsg) {
	$(window).scrollTop(0);
	var msgArea = $('#altError').children("div");
	msgArea.empty();
	var hasMsg = false;
	$.each(arrMsg, function(idx, val) {
		msgArea.append('<div>' + val + '</div>');
		hasMsg = true;
	});
	if (hasMsg)
		$('#altError').fadeIn(150);
	else
		$('#altError').fadeOut(0);
}

function showAlertInfo(arrMsg) {
	$(window).scrollTop(0);
	var msgArea = $('#altInfo').children("div");
	msgArea.empty();
	var hasMsg = false;
	$.each(arrMsg, function(idx, val) {
		msgArea.append('<div>' + val + '</div>');
		hasMsg = true;
	});
	if (hasMsg)
		$('#altInfo').fadeIn(150);
	else
		$('#altInfo').fadeOut(0);
}

function showAlertWarn(arrMsg) {
	$(window).scrollTop(0);
	var msgArea = $('#altWarn').children("div");
	msgArea.empty();
	var hasMsg = false;
	$.each(arrMsg, function(idx, val) {
		msgArea.append('<div>' + val + '</div>');
		hasMsg = true;
	});
	if (hasMsg)
		$('#altWarn').fadeIn(150);
	else
		$('#altWarn').fadeOut(0);
}
