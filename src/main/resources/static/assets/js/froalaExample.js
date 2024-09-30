// Froala Editor를 초기화하고 이미지 및 파일 업로드 설정을 구성합니다.
const editorInstance = new FroalaEditor('#editor', {
    // 에디터의 언어를 한국어로 설정합니다.
    language: 'ko',

    // 이미지 업로드를 처리할 서버의 URL을 설정합니다.
    imageUploadURL: '/froala/upload_image',

    // 파일 업로드를 처리할 서버의 URL을 설정합니다.
    fileUploadURL: '/froala/upload_file',

    // 허용되는 이미지 파일의 형식을 설정합니다.
    imageAllowedTypes: ['jpeg', 'jpg', 'png', 'gif'],

    // 허용되는 일반 파일(첨부파일)의 형식을 설정합니다.
    fileAllowedTypes: [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ],

    // 파일 업로드 시 추가적으로 전달할 파라미터를 설정합니다.
    fileUploadParams: {
        id: 'my_editor'  // 예시로 'id' 파라미터를 설정
    },

    // 이벤트 핸들러를 설정합니다.
    events: {
        // 파일이 성공적으로 업로드되었을 때 호출됩니다.
        'file.uploaded': function (response) {
            // 서버로부터의 응답을 파싱하여 파일의 링크를 가져옵니다.
            const responseObj = JSON.parse(response);
            const fileLink = responseObj.link;

            // 업로드된 파일의 링크를 에디터에 삽입합니다.
            editorInstance.html.insert(`<a href="${fileLink}" target="_blank">첨부파일</a>`);
        }
    }
});

// '저장' 버튼에 클릭 이벤트 리스너를 추가합니다.
document.getElementById('save-button').addEventListener('click', function () {
    // 에디터의 내용을 가져옵니다.
    const content = editorInstance.html.get().trim();

    // 내용이 비어 있거나 공백만 있는 경우 저장하지 않음
    if (!content || content === "<p><br></p>") {
        alert('내용을 입력해주세요.');
        return;  // 함수 종료, 저장 요청을 보내지 않음
    }

    // 서버로 콘텐츠를 저장하기 위한 POST 요청을 보냅니다.
    fetch('/content/save', {
        method: 'POST',  // HTTP 메서드를 POST로 설정
        headers: {
            'Content-Type': 'application/json'  // 요청의 콘텐츠 유형을 JSON으로 설정
        },
        // 요청의 본문에 에디터의 내용을 JSON 형식으로 포함시킵니다.
        body: JSON.stringify({ content: content })
    })
        // 서버의 응답을 처리합니다.
        .then(response => response.json())
        .then(data => {
            if (data.id) {
                // 콘텐츠가 성공적으로 저장되면 확인 메시지를 표시하고, 보기 페이지로 리디렉션합니다.
                alert('글쓰기에 성공하였습니다!');
                window.location.href = 'view.html?id=' + data.id;  // 본인의 페이지에 맞게 이동, 기본값 = 'view.html?id=' + data.id
            } else {
                // 저장에 실패한 경우 경고 메시지를 표시합니다.
                alert('글쓰기에 실패하였습니다.');
            }
        })
        .catch(error => {
            // 요청 중 오류가 발생한 경우 오류 메시지를 콘솔에 출력하고, 경고 메시지를 표시합니다.
            console.error('Error:', error);
            alert('글쓰기에 실패하였습니다.');
        });
});