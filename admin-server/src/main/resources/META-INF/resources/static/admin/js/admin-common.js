// 관리자 공통 JavaScript

$(document).ready(function() {
    // 초기화
    initializeAdmin();
    updateSystemTime();
    
    // 1분마다 시스템 시간 업데이트
    setInterval(updateSystemTime, 60000);
});

// 관리자 초기화
function initializeAdmin() {
    checkAuth();
    setActiveMenuItem();
}

// 인증 체크
function checkAuth() {
    const token = localStorage.getItem('admin_token');
    if (!token) {
        window.location.href = '/admin/login';
        return;
    }
    
    // 토큰 유효성 검사 (선택사항)
    /*
    $.ajax({
        url: '/admin/api/auth/check',
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        error: function() {
            localStorage.removeItem('admin_token');
            window.location.href = '/admin/login';
        }
    });
    */
}

// 현재 페이지에 맞는 메뉴 활성화
function setActiveMenuItem() {
    const currentPath = window.location.pathname;
    $('.sidebar-link').removeClass('active');
    
    if (currentPath.includes('/dashboard')) {
        $('.sidebar-link[href="/admin/dashboard"]').addClass('active');
    } else if (currentPath.includes('/news')) {
        $('.sidebar-link[href="/admin/news"]').addClass('active');
    } else if (currentPath.includes('/quiz')) {
        $('.sidebar-link[href="/admin/quiz"]').addClass('active');
    } else if (currentPath.includes('/users')) {
        $('.sidebar-link[href="/admin/users"]').addClass('active');
    }
}

// 로그아웃 처리
function handleLogout() {
    if (confirm('정말 로그아웃 하시겠습니까?')) {
        const token = localStorage.getItem('admin_token');
        
        $.ajax({
            url: '/admin/api/auth/logout',
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            complete: function() {
                localStorage.removeItem('admin_token');
                showToast('로그아웃되었습니다.', 'success');
                setTimeout(function() {
                    window.location.href = '/admin/login';
                }, 1000);
            }
        });
    }
}

// 시스템 시간 업데이트
function updateSystemTime() {
    const now = new Date();
    const timeString = now.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        weekday: 'short'
    });
    $('#systemTime').text(timeString);
}

// 토스트 메시지 표시
function showToast(message, type = 'info') {
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'primary'} border-0 position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999;" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-triangle' : 'info-circle'} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    $('body').append(toastHtml);
    const toastElement = $('.toast').last();
    const toast = new bootstrap.Toast(toastElement[0], { delay: 3000 });
    toast.show();
    
    // 토스트가 숨겨진 후 DOM에서 제거
    toastElement[0].addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}

// 데이터 로딩 표시
function showLoading(target = 'body') {
    const loadingHtml = `
        <div class="loading-overlay position-absolute w-100 h-100 d-flex align-items-center justify-content-center" 
             style="background: rgba(255,255,255,0.8); z-index: 1000; top: 0; left: 0;">
            <div class="text-center">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">로딩 중...</span>
                </div>
                <div class="mt-2">데이터 로딩 중...</div>
            </div>
        </div>
    `;
    $(target).css('position', 'relative').append(loadingHtml);
}

// 로딩 숨기기
function hideLoading(target = 'body') {
    $(target).find('.loading-overlay').remove();
}

// 숫자 포맷팅 (천단위 콤마)
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// 날짜 포맷팅
function formatDate(date) {
    return new Date(date).toLocaleDateString('ko-KR');
}

// 날짜시간 포맷팅
function formatDateTime(date) {
    return new Date(date).toLocaleString('ko-KR');
}

// 페이지 새로고침
function refreshPage() {
    showLoading();
    setTimeout(function() {
        location.reload();
    }, 500);
}

// 확인 다이얼로그
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

// 테이블 행 클릭 이벤트
function handleTableRowClick(selector, callback) {
    $(document).on('click', selector, function(e) {
        e.preventDefault();
        callback(this);
    });
}

// AJAX 에러 처리
function handleAjaxError(xhr, status, error) {
    console.error('AJAX Error:', status, error);
    
    let message = '요청 처리 중 오류가 발생했습니다.';
    if (xhr.status === 401) {
        message = '인증이 만료되었습니다. 다시 로그인해주세요.';
        localStorage.removeItem('admin_token');
        setTimeout(function() {
            window.location.href = '/admin/login';
        }, 2000);
    } else if (xhr.status === 403) {
        message = '접근 권한이 없습니다.';
    } else if (xhr.status === 404) {
        message = '요청한 리소스를 찾을 수 없습니다.';
    } else if (xhr.status >= 500) {
        message = '서버 오류가 발생했습니다.';
    }
    
    showToast(message, 'error');
}

// 공통 AJAX 설정 + 자동 리프레시/재시도
(function() {
    let isRefreshing = false;
    let pendingRequests = [];

    function queueRequest(settings, deferred) {
        pendingRequests.push({ settings: settings, deferred: deferred });
    }

    function retryPending() {
        const queue = pendingRequests.slice();
        pendingRequests = [];
        queue.forEach(({ settings, deferred }) => {
            $.ajax(settings)
                .done(function(data, textStatus, jqXHR) { deferred.resolve(data, textStatus, jqXHR); })
                .fail(function(jqXHR, textStatus, errorThrown) { deferred.reject(jqXHR, textStatus, errorThrown); });
        });
    }

    function refreshToken() {
        return $.ajax({
            url: '/admin/api/auth/refresh',
            method: 'POST',
            // 쿠키 기반이므로 본문/헤더 불필요
        });
    }

    // 모든 요청의 error 콜백을 래핑하여 401 시 자동 리프레시/재시도를 강제
    // 전역 401 처리: 모든 AJAX 에러를 한 곳에서 가로채자
    $(document).ajaxError(function(event, jqXHR, settings) {
        if (jqXHR && jqXHR.status === 401) {
            // 원 요청 보관 후, 리프레시 수행
            const dfd = $.Deferred();
            const retrySettings = $.extend(true, {}, settings);
            queueRequest(retrySettings, dfd);

            if (!isRefreshing) {
                isRefreshing = true;
                refreshToken()
                    .done(function(resp) {
                        if (resp && resp.accessToken) {
                            localStorage.setItem('admin_token', resp.accessToken);
                        }
                        retryPending();
                    })
                    .fail(function() {
                        pendingRequests = [];
                        localStorage.removeItem('admin_token');
                        setTimeout(function() { window.location.href = '/admin/login'; }, 500);
                    })
                    .always(function() { isRefreshing = false; });
            }
        }
    });

    $.ajaxSetup({
        beforeSend: function(xhr) {
            const token = localStorage.getItem('admin_token');
            if (token) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
            // AJAX 식별을 명시적으로 추가 (서버 엔트리포인트가 AJAX를 구분할 수 있게)
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
        },
        error: function(xhr, status, error) {
            // 401은 전역 ajaxError에서 처리하므로 여기서는 일반 에러만 처리
            if (xhr && xhr.status === 401) return;
            handleAjaxError(xhr, status, error);
        }
    });
})();
