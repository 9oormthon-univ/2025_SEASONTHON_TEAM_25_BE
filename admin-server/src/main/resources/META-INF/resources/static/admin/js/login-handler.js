// 로그인 JavaScript
$(document).ready(function() {
    checkAuthStatus();
    
    $('#loginForm').on('submit', function(e) {
        e.preventDefault();
        handleLogin();
    });

    $('#email, #password').on('keypress', function(e) {
        if (e.which === 13) {
            handleLogin();
        }
    });
});

function checkAuthStatus() {
    const token = localStorage.getItem('admin_token');
    if (token) {
        window.location.href = '/admin/dashboard';
    }
}

function handleLogin() {
    const email = $('#email').val().trim();
    const password = $('#password').val();
    
    if (!email || !password) {
        showAlert('이메일과 비밀번호를 모두 입력해주세요.', 'danger');
        return;
    }

    setLoading(true);

    $.ajax({
        url: '/admin/api/auth/login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ email: email, password: password }),
        success: function(response) {
            if (response.success) {
                localStorage.setItem('admin_token', response.accessToken);
                showAlert('로그인 성공! 대시보드로 이동합니다...', 'success');
                setTimeout(function() {
                    window.location.href = '/admin/dashboard';
                }, 1000);
            } else {
                showAlert(response.message || '로그인에 실패했습니다.', 'danger');
            }
        },
        error: function(xhr) {
            var message = '로그인에 실패했습니다.';
            showAlert(message, 'danger');
        },
        complete: function() { setLoading(false); }
    });
}

function setLoading(loading) {
    const btn = $('#loginBtn');
    const btnText = btn.find('.btn-text');
    const btnLoading = btn.find('.btn-loading');
    
    if (loading) {
        btn.prop('disabled', true);
        btnText.addClass('d-none');
        btnLoading.removeClass('d-none');
    } else {
        btn.prop('disabled', false);
        btnText.removeClass('d-none');
        btnLoading.addClass('d-none');
    }
}

function showAlert(message, type) {
    var iconClass = (type === 'danger') ? 'exclamation-triangle' : 'check-circle';
    const alertHtml = '<div class="alert alert-' + type + '" role="alert">' +
        '<i class="fas fa-' + iconClass + ' me-2"></i>' + message + '</div>';
    
    $('#alertMessage').html(alertHtml);
}
