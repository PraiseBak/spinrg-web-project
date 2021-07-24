package com.mindaces.mindaces.service;

import com.mindaces.mindaces.domain.Role;
import com.mindaces.mindaces.domain.entity.User;
import com.mindaces.mindaces.domain.repository.UserRepository;
import com.mindaces.mindaces.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService
{
    private UserRepository userRepository;

    @Transactional
    public Long joinUser(UserDto userDto)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setUserID(userDto.getUserID());
        userDto.setUserEmail(userDto.getUserEmail());
        userDto.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
        return userRepository.save(userDto.toEntity()).getUserIdx();
    }

    @Override
    public UserDetails loadUserByUsername(String userID) throws UsernameNotFoundException
    {
        //Optional<User> userEntityWrapper = userRepository.findByUserEmail(userEmail);
        Optional<User> userEntityWrapper = userRepository.findByUserID(userID);
        User user = userEntityWrapper.get();
        List<GrantedAuthority> authorities = new ArrayList<>();

        if(("praisebak@naver.com").equals(userID))
        {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        }
        else
        {
            authorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
        }
        return new org.springframework.security.core.userdetails.
                User(user.getUserID(),user.getUserPassword(),authorities);
    }



}
