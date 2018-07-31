$(function() {

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
