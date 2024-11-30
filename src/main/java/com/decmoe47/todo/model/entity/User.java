package com.decmoe47.todo.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@Entity
public class User extends BaseEntity<Integer> implements UserDetails {

    private String email;
    private String password;
    private String name;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private boolean isLocked = false;
    private LocalDateTime registerTime = LocalDateTime.now();
    private LocalDateTime accountExpireTime = null;
    private LocalDateTime credentialExpireTime = LocalDateTime.now().plusDays(30);

    @OneToMany
    private List<Member> members = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> result = new ArrayList<>();
        for (Member member : members) {
            result.addAll(member.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.getTeamId() + "_" + r.getPrivilegeString()))
                    .toList()
            );
        }
        return result;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialExpireTime != null && LocalDateTime.now().isBefore(credentialExpireTime);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
