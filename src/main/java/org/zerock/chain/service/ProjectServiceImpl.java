package org.zerock.chain.service;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.chain.dto.ProjectDTO;
import org.zerock.chain.dto.ProjectRequestDTO;
import org.zerock.chain.model.Project;
import org.zerock.chain.repository.ProjectRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl extends BaseService<Project> implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Override
    protected List<Project> getAllItemsByEmpNo(Long empNo) {
        return projectRepository.findByEmpNo(empNo);
    }


    // 진행중인 프로젝트 목록
    @Override
    public List<ProjectDTO> getOngoingProjects() {
        // 프로젝트 진행도가 100 미만인 프로젝트들만 반환
        return projectRepository.findAll()
                .stream()
                .filter(project -> project.getProjectProgress() < 100)
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
    }


    @Override // 프로젝트 생성
    public Long register(ProjectDTO projectDTO) {
        // 세션에서 사원번호(empNo) 가져오기
        Long empNo = getEmpNoFromSession();
        // DTO에서 엔티티로 매핑
        Project project = modelMapper.map(projectDTO, Project.class);
        // 가져온 사원번호를 엔티티에 설정
        project.setEmpNo(empNo);
        log.info("Mapped Project entity: " + project);

        // Process progress labels 리스트로 라벨 저장
        String labelsString = String.join(",",
                projectDTO.getProgressLabel20(),
                projectDTO.getProgressLabel40(),
                projectDTO.getProgressLabel60(),
                projectDTO.getProgressLabel80(),
                projectDTO.getProgressLabel100()
        );
        project.setProgressLabels(labelsString);

        // 파일 정보가 있을 경우, 파일 경로와 이름을 설정
        try {
            if (projectDTO.getProjectFiles() != null && !projectDTO.getProjectFiles().isEmpty()) {
                // 파일 업로드 및 경로 생성
                String projectFile = uploadFile(projectDTO.getProjectFiles());
                project.setProjectFile(projectFile);  // 파일 경로와 이름을 엔티티에 저장
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 파일 업로드 실패 시 적절한 처리 (예: 에러 페이지로 리턴)
            return null;
        }

        // 저장 및 프로젝트 번호 반환
        Long projectNo = projectRepository.save(project).getProjectNo();
        return projectNo;
    }

    @Override// 프로젝트 수정
    public void updateProject(Long projectNo, ProjectRequestDTO projectRequestDTO) {
        Optional<Project> result = projectRepository.findById(projectNo);
        Project existingProject = result.orElseThrow(() -> new NoSuchElementException("Project not found"));

        // 기존 사원번호(empNo)를 유지
        Long existingEmpNo = existingProject.getEmpNo();
        projectRequestDTO.setEmpNo(existingEmpNo);

        // 기존 uploadDate를 유지
        if (!existingProject.isTemporary()) {
            projectRequestDTO.setUploadDate(existingProject.getUploadDate());
        }

        // Process progress labels 리스트로 라벨 저장
        String labelsString = String.join(",",
                projectRequestDTO.getProgressLabel20(),
                projectRequestDTO.getProgressLabel40(),
                projectRequestDTO.getProgressLabel60(),
                projectRequestDTO.getProgressLabel80(),
                projectRequestDTO.getProgressLabel100()
        );
        projectRequestDTO.setProgressLabels(labelsString);

        // DTO를 엔티티로 매핑하여 업데이트
        modelMapper.map(projectRequestDTO, existingProject);

        // 업데이트된 프로젝트 저장
        projectRepository.save(existingProject);
    }


    @Override
    public List<ProjectDTO> getTemporaryProjects() {
        Long empNo = getEmpNoFromSession();
        List<Project> projects = projectRepository.findByEmpNoAndIsTemporary(empNo, true);
        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        Long empNo = getEmpNoFromSession();
        List<Project> projects = getItemsByEmpNo(empNo, projectRepository::findByEmpNo);
        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void setProjectFavorite(Long projectNo, boolean projectFavorite) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로젝트 ID"));
        project.setProjectFavorite(projectFavorite);
        projectRepository.save(project);
    }

    @Override
    public void updateProjectProgress(Long projectNo, Integer projectProgress) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로젝트 ID"));
        project.setProjectProgress(projectProgress);
        projectRepository.save(project);
    }

    @Override
    public ProjectDTO getProjectById(Long projectNo) {
        Optional<Project> result = projectRepository.findById(projectNo);
        Project project = result.orElseThrow(() -> new NoSuchElementException("Project not found"));
        return modelMapper.map(project, ProjectDTO.class);
    }

    @Override
    public void deleteProject(Long projectNo) {
        projectRepository.deleteById(projectNo);
    }

    @Override
    public String saveFile(MultipartFile file) throws Exception {
        String uploadDir = "uploads/";
        String originalFilename = file.getOriginalFilename();
        String filePath = uploadDir + originalFilename;
        File destinationFile = new File(filePath);

        // 업로드 파일 존재 확인
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        file.transferTo(destinationFile);
        return filePath;
    }

    // 세션에 저장된 사원번호 가져오기
    private Long getEmpNoFromSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        return (Long) session.getAttribute("empNo");
    }

    @Override
    public List<ProjectDTO> getProjectsByUser(Long empNo) {
        // 사용자(empNo)에 따라 프로젝트를 조회하고, DTO로 변환하여 반환
        return projectRepository.findByEmpNo(empNo).stream()
                .map(project -> modelMapper.map(project, ProjectDTO.class))  // 엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String savedFileName = UUID.randomUUID()+ "_" + originalFileName;
            Path filePath = Paths.get(UPLOAD_DIR, savedFileName).normalize();

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 파일 이름과 경로 결합하여 반환
            return originalFileName + "|" + filePath;
        }
        return null;
    }
}
