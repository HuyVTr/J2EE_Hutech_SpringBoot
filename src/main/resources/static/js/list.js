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
                    // Chỉ hiển thị nút Edit/Delete nếu là ADMIN
                    if (typeof isAdmin !== 'undefined' && isAdmin) {
                        actionButtons += '<a href="/books/edit/' + item.id + '" class="btn btn-primary btn-sm me-1">Edit</a>';
                        actionButtons += '<button onclick="apiDeleteBook(' + item.id + ')" class="btn btn-danger btn-sm me-1">Delete</button>';
                    }
                    // Nút Add to Cart hiển thị cho tất cả
                    actionButtons += '<a href="/cart/add/' + item.id + '" class="btn btn-success btn-sm">Add to Cart</a>';

                    trHTML += '<tr id="book-' + item.id + '">' +
                        '<td>' + item.id + '</td>' +
                        '<td>' + item.title + '</td>' +
                        '<td>' + item.author + '</td>' +
                        '<td>' + item.price + '</td>' +
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
