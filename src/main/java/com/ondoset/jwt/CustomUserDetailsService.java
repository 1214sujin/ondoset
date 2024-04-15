package com.ondoset.jwt;

import com.ondoset.domain.Member;
import com.ondoset.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public CustomUserDetails loadUserByUsername(String name) throws UsernameNotFoundException {

		Member member = memberRepository.findByName(name);

		if (member != null) {
			return new CustomUserDetails(member);
		}

		return null;
	}
}
