var render = function(theme,data,meta,require) {
	theme('2-column-right',{
		title:'Landing page test',
		header:[{
			partial:'header',
			context:data
		}],
		body:[{
			partial:'gc-landing',
			context:data
		}
		]
	});
};