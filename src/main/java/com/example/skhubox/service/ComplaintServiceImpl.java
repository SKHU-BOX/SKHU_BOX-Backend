package com.example.skhubox.service;

import com.example.skhubox.domain.complaint.Complaint;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.complaint.ComplaintAnswerRequest;
import com.example.skhubox.dto.complaint.ComplaintRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(String studentNumber, ComplaintRequest request) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Complaint complaint = Complaint.builder()
                .user(user)
                .lockerNumber(request.getLockerNumber())
                .content(request.getContent())
                .build();

        complaintRepository.save(complaint);
        return ComplaintResponse.of(complaint);
    }

    @Override
    public List<ComplaintResponse> getMyComplaints(String studentNumber) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return complaintRepository.findByUserId(user.getId()).stream()
                .map(ComplaintResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponse answerComplaint(Long complaintId, ComplaintAnswerRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        complaint.answerComplaint(request.getStatus(), request.getAnswer());
        return ComplaintResponse.of(complaint);
    }
}
