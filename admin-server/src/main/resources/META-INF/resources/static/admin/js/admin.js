// 관리자 페이지 공통 JavaScript

$(document).ready(function() {
    // 전역 AJAX 설정
    setupAjaxDefaults();
    
    // 현재 페이지에 해당하는 사이드바 메뉴 활성화
    highlightCurrentMenu();
    
    // 토큰 만료 체크
    checkTokenExpiry();
});

/**
 * AJAX 기본 설정
 */
function setupAjaxDefaults() {
    // 모든 AJAX 요청에 Authorization 헤더 자동 추가
    $.ajaxSetup({
        beforeSend: function(xhr) {
            const token = localStorage.getItem('admin_token');
            if (token) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
        },
        error: function(xhr, status, error) {
            // 401 오류 시 로그인 페이지로 리다이렉트
            console.log('AJAX Error:', status, error);
            if (xhr.status === 401) {
                alert('로그인이 필요합니다.');
                window.location.href = '/admin/login';
            } else if (xhr.status === 403) {
                alert('권한이 없습니다.');
            } else if (xhr.status >= 500) {
                alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            }
        }
    });
}

/**
 * 현재 페이지 메뉴 하이라이트
 */
function highlightCurrentMenu() {
    const currentPath = window.location.pathname;
    $('.sidebar-link').each(function() {
        const href = $(this).attr('href');
        if (currentPath.startsWith(href) && href !== '/admin/dashboard') {
            $(this).addClass('active');
        } else if (currentPath === '/admin/dashboard' && href === '/admin/dashboard') {
            $(this).addClass('active');
        }
    });
}

/**
 * 토큰 만료 체크
 */
function checkTokenExpiry() {
    const token = localStorage.getItem('admin_token');
    if (!token) {
        if (window.location.pathname !== '/admin/login') {
            window.location.href = '/admin/login';
        }
        return;
    }
    
    // JWT 토큰 디코딩하여 만료시간 확인
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        
        // 토큰이 5분 이내에 만료될 예정이면 갱신
        if (payload.exp - currentTime < 300) {
            refreshToken();
        }
    } catch (e) {
        console.error('토큰 파싱 오류:', e);
    }
}

/**
 * 토큰 갱신
 */
function refreshToken() {
    $.ajax({
        url: '/admin/api/auth/refresh',
        method: 'POST',
        success: function(response) {
            if (response.success && response.accessToken) {
                localStorage.setItem('admin_token', response.accessToken);
            }
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                window.location.href = '/admin/login';
            }
        }
    });
}

/**
 * 관리자 로그아웃
 */
function adminLogout() {
    if (confirm('정말 로그아웃 하시겠습니까?')) {
        $.ajax({
            url: '/admin/api/auth/logout',
            method: 'POST',
            success: function(response) {
                localStorage.removeItem('admin_token');
                window.location.href = '/admin/login';
            },
            error: function(xhr) {
                // 실패해도 로컬 토큰 삭제하고 로그인 페이지로
                localStorage.removeItem('admin_token');
                window.location.href = '/admin/login';
            }
        });
    }
}

/**
 * 페이지네이션 처리
 */
function handlePagination(currentPage, totalPages, onPageChange) {
    const paginationHtml = generatePaginationHtml(currentPage, totalPages);
    $('#pagination').html(paginationHtml);
    
    // 페이지 클릭 이벤트 바인딩
    $('#pagination .page-link').on('click', function(e) {
        e.preventDefault();
        const page = $(this).data('page');
        if (page !== undefined && page !== currentPage) {
            onPageChange(page);
        }
    });
}

/**
 * 페이지네이션 HTML 생성
 */
function generatePaginationHtml(currentPage, totalPages) {
    if (totalPages <= 1) return '';
    
    let html = '<nav><ul class="pagination justify-content-center">';
    
    // 이전 버튼
    html += `<li class="page-item ${currentPage === 0 ? 'disabled' : ''}">`;
    html += `<a class="page-link" href="#" data-page="${currentPage - 1}">이전</a>`;
    html += '</li>';
    
    // 페이지 번호들
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        html += `<li class="page-item ${i === currentPage ? 'active' : ''}">`;
        html += `<a class="page-link" href="#" data-page="${i}">${i + 1}</a>`;
        html += '</li>';
    }
    
    // 다음 버튼
    html += `<li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">`;
    html += `<a class="page-link" href="#" data-page="${currentPage + 1}">다음</a>`;
    html += '</li>';
    
    html += '</ul></nav>';
    return html;
}

/**
 * 로딩 상태 표시/숨김
 */
function showLoading(target) {
    const $target = $(target);
    $target.html(`
        <div class="text-center p-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">로딩중...</span>
            </div>
            <div class="mt-2">데이터를 불러오는 중...</div>
        </div>
    `);
}

function hideLoading(target) {
    // 구현체에서 실제 데이터로 교체
}

/**
 * 에러 메시지 표시
 */
function showError(message, target) {
    const $target = $(target);
    $target.html(`
        <div class="alert alert-danger text-center">
            <i class="fas fa-exclamation-triangle"></i>
            ${message}
        </div>
    `);
}

/**
 * 성공 토스트 메시지
 */
function showSuccessToast(message) {
    // Bootstrap Toast 또는 간단한 알림
    const toastHtml = `
        <div class="position-fixed top-0 end-0 p-3" style="z-index: 11000">
            <div class="toast show bg-success text-white" role="alert">
                <div class="toast-body">
                    <i class="fas fa-check-circle me-2"></i>
                    ${message}
                </div>
            </div>
        </div>
    `;
    
    $('body').append(toastHtml);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        $('.toast').fadeOut(() => {
            $('.toast').parent().remove();
        });
    }, 3000);
}

/**
 * 에러 토스트 메시지
 */
function showErrorToast(message) {
    const toastHtml = `
        <div class="position-fixed top-0 end-0 p-3" style="z-index: 11000">
            <div class="toast show bg-danger text-white" role="alert">
                <div class="toast-body">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    ${message}
                </div>
            </div>
        </div>
    `;
    
    $('body').append(toastHtml);
    
    setTimeout(() => {
        $('.toast').fadeOut(() => {
            $('.toast').parent().remove();
        });
    }, 4000);
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString, includeTime = true) {
    const date = new Date(dateString);
    const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    };
    
    if (includeTime) {
        options.hour = '2-digit';
        options.minute = '2-digit';
        options.hour12 = false;
    }
    
    return date.toLocaleDateString('ko-KR', options);
}

/**
 * 상대적 시간 표시 (예: "5분 전", "2시간 전")
 */
function getRelativeTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return '방금 전';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`;
    if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}일 전`;
    
    return formatDate(dateString, false);
}

/**
 * 문자열 자르기
 */
function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

/**
 * 숫자 포맷팅 (천단위 콤마)
 */
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * 확인 다이얼로그
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * 폼 데이터를 JSON으로 변환
 */
function formToJSON(form) {
    const formData = new FormData(form);
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }
    
    return data;
}

// 전역 유틸리티 함수들을 window 객체에 등록
window.adminUtils = {
    showLoading,
    hideLoading,
    showError,
    showSuccessToast,
    showErrorToast,
    formatDate,
    getRelativeTime,
    truncateText,
    formatNumber,
    confirmAction,
    formToJSON,
    handlePagination
};
