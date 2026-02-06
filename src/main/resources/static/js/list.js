$(document).ready(function () {
    // Load danh sách mặc định khi trang tải xong
    loadBooks('/api/v1/books');

    // Xử lý sự kiện khi submit form tìm kiếm
    $('#search-form').submit(function (e) {
        e.preventDefault(); // Ngăn chặn reload trang
        let keyword = $('input[name="keyword"]').val();
        if (keyword) {
            loadBooks('/api/v1/books/search?keyword=' + encodeURIComponent(keyword));
        } else {
            loadBooks('/api/v1/books');
        }
    });

    // Xử lý sự kiện khi bấm nút Refresh
    $('#refresh-btn').click(function () {
        $('input[name="keyword"]').val(''); // Xóa từ khóa trong ô input
        loadBooks('/api/v1/books'); // Load lại danh sách đầy đủ
    });

    // Hàm load dữ liệu (có thể tái sử dụng)
    function loadBooks(apiUrl) {
        $.ajax({
            url: apiUrl,
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                let trHTML = '';
                $.each(data, function (i, item) {
                    // Xử lý hiển thị tên Category an toàn (nếu là object hoặc string)
                    let categoryName = 'None';
                    if (item.category) {
                        categoryName = item.category.name ? item.category.name : item.category;
                    }

                    let actionButtons = '';

                    // Check quyền quản lý (Admin hoặc Staff) - hỗ trợ cả biến cũ isAdmin và mới canManage
                    let isManager = (typeof canManage !== 'undefined' && canManage) || (typeof isAdmin !== 'undefined' && isAdmin);

                    if (isManager) {
                        // Nút Kho
                        actionButtons += `<button onclick="openStockModal(${item.id}, '${item.title.replace(/'/g, "\\'")}', ${item.quantity || 0})" class="btn btn-outline-warning action-btn me-1"><i class="bi bi-box-seam"></i> Kho</button>`;
                        // Nút Sửa
                        actionButtons += '<a href="/books/edit/' + item.id + '" class="btn btn-outline-primary action-btn me-1">Sửa</a>';
                        // Nút Xóa
                        actionButtons += '<button onclick="apiDeleteBook(' + item.id + ')" class="btn btn-outline-danger action-btn me-1">Xóa</button>';
                    }

                    // Nút Chi tiết (Ai cũng thấy)
                    actionButtons += '<a href="/books/detail/' + item.id + '" class="btn btn-info action-btn text-white me-1">Chi tiết</a>';

                    // Nút Thêm vào giỏ (Chỉ User thường hoặc Khách thấy)
                    if (!isManager) {
                        actionButtons += '<a href="/cart/add/' + item.id + '" class="btn btn-success action-btn text-white">Thêm vào giỏ</a>';
                    }

                    trHTML += '<tr id="book-' + item.id + '">' +
                        '<td>' + item.id + '</td>' +
                        '<td>' + item.title + '</td>' +
                        '<td>' + (item.imagePath ? '<img src="' + item.imagePath + '" style="max-height: 50px;" />' : '') + '</td>' +
                        '<td>' + item.author + '</td>' +
                        '<td>' + new Intl.NumberFormat('vi-VN').format(item.price) + ' VNĐ</td>' +
                        '<td>' + (item.quantity !== null ? item.quantity : 0) + '</td>' +
                        '<td>' + categoryName + '</td>' +
                        '<td>' + actionButtons + '</td>' +
                        '</tr>';
                });
                // Chèn các dòng dữ liệu vào bảng
                $('#book-table-body').empty(); // Xóa dữ liệu cũ trước khi thêm mới
                $('#book-table-body').append(trHTML);
            },
            error: function (xhr, status, error) {
                console.error("Lỗi khi gọi API:", error);
                alert("Không thể tải danh sách sách từ API!");
            }
        });
    }
});

// Hàm xóa sách sử dụng API DELETE
function apiDeleteBook(id) {
    if (confirm('Bạn có chắc chắn muốn xóa cuốn sách này?')) {
        $.ajax({
            url: '/api/v1/books/' + id,
            type: 'DELETE',
            success: function () {
                alert('Xóa sách thành công!');
                // Xóa dòng tương ứng trên giao diện mà không cần reload trang
                $('#book-' + id).remove();
            },
            error: function (xhr, status, error) {
                alert('Lỗi khi xóa sách: ' + xhr.status);
                console.error(error);
            }
        });
    }
}

// Hàm mở Modal quản lý kho
function openStockModal(id, title, quantity) {
    $('#stockBookId').val(id);
    $('#stockBookTitle').val(title);
    $('#currentStock').val(quantity);
    $('#changeAmount').val(''); // Reset input

    // Sử dụng Bootstrap Modal API
    var stockModal = new bootstrap.Modal(document.getElementById('stockModal'));
    stockModal.show();
}
