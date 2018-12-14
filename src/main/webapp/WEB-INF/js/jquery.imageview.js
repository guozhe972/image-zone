(function($) {
	function ImageView(obj, opts) {
		this.init = function(obj, opts) {
			this.$obj = $(obj);
			this.$targets = $(opts.targetSelector, this.$obj);
			this.opts = opts;
			this.i = 0;
			this.idx = 0;
			this.x = 0;
			this.y = 0;
			this.last = this.$targets.length - 1;
			this.deploy();
		}

		this.deploy = function() {
			var html = '<div class="imageview">'
					+ '<div class="title"></div>'
					+ '<a href="javascript:;" class="cart"></a>'
					+ '<a href="javascript:;" class="down"></a>'
					+ '<a href="javascript:;" class="hide"></a>'
					+ '<a href="javascript:;" class="prev"></a>'
					+ '<a href="javascript:;" class="next"></a>'
					+ '<div class="image"><img src="" />'
					+ '<video src="" poster="" preload="metadata" type="video/mp4" playsinline controls />'
					+ '</div></div>';
			this.$viewer = $(html).appendTo('body');
			this.$title = $('.title', this.$viewer);
			this.$cart = $('.cart', this.$viewer);
			this.$down = $('.down', this.$viewer);
			this.$hide = $('.hide', this.$viewer);
			this.$prev = $('.prev', this.$viewer);
			this.$next = $('.next', this.$viewer);
			this.$image = $('.image img', this.$viewer);
			this.$video = $('.image video', this.$viewer);
			this.$cart.click($.proxy(this.cart, this));
			this.$down.click($.proxy(this.down, this));
			this.$hide.click($.proxy(this.hide, this));
			this.$prev.click($.proxy(this.prev, this));
			this.$next.click($.proxy(this.next, this));
			this.$image.click($.proxy(this.hide, this));
			this.$viewer.on('touchstart', $.proxy(this.touchstart, this));
			this.$viewer.on('touchend', $.proxy(this.touchend, this));
			this.$image.bind('load', function() {
				$(this).fadeIn(100);
			});
			this.$video.bind('loadedmetadata', function() {
				$(this).fadeIn(100);
			});
			$('body').keydown($.proxy(this.keydown, this));
			var self = this;
			this.$targets.each(function(i) {
				$(this).click(function(e) {
					$('body').addClass('iv-open');
					self.show(i);
					return false;
				});
			});
		}

		this.reload = function() {
			this.$targets = $(this.opts.targetSelector, this.$obj);
			this.last = this.$targets.length - 1;
			if (this.i > this.last) {
				this.i = this.last;
				if (this.i < 0)
					this.i = 0;
			}
			var self = this;
			this.$targets.each(function(i) {
				$(this).off('click');
				$(this).click(function(e) {
					$('body').addClass('iv-open');
					self.show(i);
					return false;
				});
			});
		}

		this.cart = function() {
			this.$cart.hide();
			$('#btnAdd' + this.idx).trigger('click');
		}

		this.down = function() {
			$('#btnDown' + this.idx).get(0).click();
		}

		this.hide = function() {
			this.$viewer.fadeOut(100);
			$('body').removeClass('iv-open');
		}

		this.keydown = function(e) {
			if (!this.$viewer.is(':visible')) {
				return;
			}
			if (e.keyCode == 37) {
				this.prev();
			}
			if (e.keyCode == 39) {
				this.next();
			}
			if (e.keyCode == 27) {
				this.hide();
			}
		}

		this.touchstart = function(e) {
			if (!this.$viewer.is(':visible')) {
				return;
			}
			this.x = Math.round(e.originalEvent.changedTouches[0].pageX);
			this.y = Math.round(e.originalEvent.changedTouches[0].pageY);
		}

		this.touchend = function(e) {
			if (!this.$viewer.is(':visible')) {
				return;
			}
			var x = Math.round(e.originalEvent.changedTouches[0].pageX);
			var y = Math.round(e.originalEvent.changedTouches[0].pageY);
			var disX = Math.abs(this.x - x);
			var disY = Math.abs(this.y - y);
			if (disY < disX) {
				var dis = Math.round(Math.sqrt(Math.pow(disX, 2)
						+ Math.pow(disY, 2)));
				if (dis > 50) {
					if (this.x < x) {
						this.prev();
					}
					if (this.x > x) {
						this.next();
					}
				}
			}
		}

		this.next = function() {
			if (this.i < this.last)
				this.show(this.i + 1);
		}

		this.prev = function() {
			if (this.i > 0)
				this.show(this.i - 1);
		}

		this.show = function(i) {
			var $target = this.$targets.eq(i);
			this.idx = $target.data('index');
			this.i = i;
			this.$prev.toggle(i > 0);
			this.$next.toggle(i < this.last);
			this.$cart.toggle($('#btnAdd' + this.idx).length > 0
					&& !$('#btnAdd' + this.idx).is(':disabled'));
			this.$down.toggle($('#btnDown' + this.idx).length > 0);
			this.$title.text($('#bagPrice' + this.idx).text());
			this.$viewer.fadeIn(100);
			var fnm = $target.attr(this.opts.srcAttr);
			if (fnm.split('.').pop() == 'mp4') {
				this.$image.hide().attr('src', '');
				this.$video.hide().attr({
					src : fnm,
					poster : fnm.replace('.mp4', '.jpg')
				});
				this.$video.get(0).load();
			} else {
				this.$video.hide().attr({
					src : '',
					poster : ''
				});
				this.$image.hide().attr('src', fnm);
			}
		}

		this.init(obj, opts);
	}

	$.fn.imageview = function(options) {
		var defaults = {
			targetSelector : 'a.img',
			srcAttr : 'href'
		};
		var opts = $.extend(defaults, options);
		return this.each(function() {
			$(this).data('imageview', new ImageView(this, opts));
		});
	}
})(jQuery);