package com.ptit.muonphong.config;

import com.ptit.muonphong.dao.NguoiDungDAO;
import com.ptit.muonphong.entity.NguoiDung;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungDAO nguoiDungDAO;

    public CustomUserDetailsService(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            NguoiDung nguoiDung = nguoiDungDAO.findByUsername(username != null ? username.trim() : "");
            if (nguoiDung == null) {
                throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
            }
            return new CustomUserDetails(nguoiDung);
        } catch (DataAccessException ex) {
            throw new AuthenticationServiceException("Không thể kết nối CSDL", ex);
        }
    }
}
