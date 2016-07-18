app.server = function (ctx) {
    return {
        endpoints: {
            pages: [
                {
                    title: 'permissions',
                    url: 'permissions',
                    path: 'permissions.jag',
                    secured: true
                }
            ]
        }
    };
};

app.pageHandlers = function (ctx) {
    return {
        onPageLoad: function () {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                ctx.res.sendRedirect(ctx.appContext + '/login');
                return false;
            }
            return true;
        }
    };
};

app.renderer = function (ctx) {
    return {
        pageDecorators: {
            populatePermissions: function (page) {
                page.rolePermissions = ['a', 'b', 'c'];
            }
        }
    }
};