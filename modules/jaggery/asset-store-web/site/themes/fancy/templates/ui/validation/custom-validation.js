$(document).ready(function() {
   $.validator.addMethod('validName', function(value, element) {
        var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
        return !illegalChars.test(value);
    }, 'The Name contains one or more illegal characters (~ ! @ # $ ; % ^ * + = { } | &lt; &gt;, \' / " \\ ) .');


});