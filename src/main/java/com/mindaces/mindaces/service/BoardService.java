package com.mindaces.mindaces.service;


import com.mindaces.mindaces.domain.entity.Board;
import com.mindaces.mindaces.domain.entity.BoardInfo;
import com.mindaces.mindaces.domain.entity.Likes;
import com.mindaces.mindaces.domain.repository.BoardWriteUserMapping;
import com.mindaces.mindaces.domain.repository.BoardRepository;
import com.mindaces.mindaces.dto.BoardDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BoardService
{
    private static final int BLOCK_PAGE_NUM_COUNT = 10;
    private static final int PAGE_POST_COUNT = 10;

    private GalleryService galleryService;
    private RoleService roleService;
    private CommentService commentService;
    private BoardRepository boardRepository;
    private UtilService utilService;

    @Transactional
    public Long savePost(BoardDto boardDto)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boardDto.setPassword(passwordEncoder.encode(boardDto.getPassword()));

        boardDto.setTitle(utilService.getTrimedStr(boardDto.getTitle()));

        Board savedBoard= boardRepository.save(boardDto.toEntity());
        Likes likes = Likes.builder()
                .contentIdx(savedBoard.getContentIdx())
                .isComment(false)
                .build();

        BoardInfo boardInfo = new BoardInfo();
        boardInfo.updateGallery(savedBoard.getGallery());
        boardInfo.updateContentIdx(savedBoard.getContentIdx());

        savedBoard.updateBoardInfo(boardInfo);
        savedBoard.updateLikes(likes);
        boardRepository.save(savedBoard);
        return 0L;
    }

    public List<BoardDto> getPostByFindObj(String findObj, Integer page, String mode)
    {
        List<BoardDto> boardDtoList = new ArrayList<>();
        Page<Board> pageEntity;

        if(mode.equals("mypage"))
        {
            pageEntity = boardRepository.findByUser(findObj,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }
        else
        {
            pageEntity = boardRepository.findByGallery(findObj,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }

        List<Board> boardList = pageEntity.getContent();

        if(boardList.isEmpty())
        {
            return boardDtoList;
        }

        for(Board board : boardList)
        {
            boardDtoList.add(this.convertEntityToDto(board));
        }
        return boardDtoList;
    }

    public Integer[] getPageList(int curPage,Long count)
    {

        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];
        Double postsTotalCount = Double.valueOf(count);
        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/ PAGE_POST_COUNT)));
        Integer blockStartPageNum =
                (curPage <= BLOCK_PAGE_NUM_COUNT / 2)
                        ? 1
                        : curPage - BLOCK_PAGE_NUM_COUNT / 2;
        Integer blockLastPageNum =
                (totalLastPageNum > blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1 )
                        ? blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1
                        : totalLastPageNum;


        for (int val = blockStartPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            pageList[idx] = val;
            if(totalLastPageNum == 1)
            {
                break;
            }
        }
        return pageList;
    }


    public BoardDto getBoardDtoByGalleryNameAndContentIdx(String galleryName,Long contentIdx)
    {
        Board board = this.getGalleryNameAndBoardIdx(galleryName,contentIdx);
        return convertEntityToDto(board);
    }

    public BoardDto convertEntityToDto(Board board)
    {
        return BoardDto.builder()
                .contentIdx(board.getContentIdx())
                .title(board.getTitle())
                .content(board.getContent())
                .gallery(board.getGallery())
                .user(board.getUser())
                .likes(board.getLikes())
                .isLoggedUser(board.getIsLoggedUser())
                .createdDate(board.getCreatedDate())
                .modifiedDate(board.getModifiedDate())
                .boardInfo(board.getBoardInfo())
                .build();
    }

    public Board getGalleryNameAndBoardIdx(String galleryName, Long contentIdx)
    {
        Board board =  boardRepository.findByGalleryAndContentIdx(galleryName,contentIdx,Board.class);
        if(board == null)
        {
            return null;
        }
        return board;
    }

    public BoardDto getBoardInfoByGalleryAndIdx(String galleryName, Long contentIdx)
    {
        Board board =  boardRepository.findByGalleryAndContentIdx(galleryName,contentIdx,Board.class);
        return this.convertEntityToDto(board);
    }

    public Long updatePost(BoardDto boardDto,String galleryName)
    {
        Board board = (Board) boardRepository.findByGalleryAndContentIdx(galleryName,boardDto.getContentIdx(),Board.class);
        board.updateContent(boardDto.getContent());
        board.updateTitle(boardDto.getTitle());
        return boardRepository.save(board).getContentIdx();
    }

    public void deletePost(Long contentIdx)
    {
        boardRepository.deleteById(contentIdx);
    }


    public Boolean isLoggedUserWriteBoard(BoardWriteUserMapping boardWriteUserMapping)
    {
        Long isLoggedUser = boardWriteUserMapping.getIsLoggedUser();
        if(isLoggedUser == 1L)
        {
            return true;
        }
        return false;
    }

    public Boolean isSameWriteUser(BoardWriteUserMapping boardWriteUserMapping,String requestUser)
    {
        String writeUser = boardWriteUserMapping.getUser();
        if(requestUser.equals(writeUser))
        {
            return true;
        }
        return false;
    }

    /*
    public BoardWriteUserMapping getBoardUserCheckMapping(String galleryName, Long contentIdx)
    {
        return boardRepository.findByGalleryAndContentIdx(galleryName,contentIdx,BoardWriteUserMapping.class);
    }

     */

    //로그인한 케이스의 게시글 수정 유효성 체크
    public Boolean isBoardModifyAuthValidLoggedUser(Authentication authentication, Long contentIdx, String galleryName)
    {
        BoardWriteUserMapping boardWriteUserMapping = boardRepository.findByGalleryAndContentIdx(galleryName,contentIdx, BoardWriteUserMapping.class);
        if(boardWriteUserMapping != null)
        {
            if(isLoggedUserWriteBoard(boardWriteUserMapping))
            {
                if(roleService.isUser(authentication))
                {
                    if(isSameWriteUser(boardWriteUserMapping,authentication.getName()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //비로그인한 케이스의 게시글 유효성 체크
    public Boolean isBoardModifyAuthValidUser(Long contentIdx, String inputPassword,String galleryName)
    {
        BoardWriteUserMapping boardWriteUserMapping = boardRepository.findByGalleryAndContentIdx(galleryName,contentIdx, BoardWriteUserMapping.class);
        if(boardWriteUserMapping.getIsLoggedUser() == 0L)
        {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            return passwordEncoder.matches(inputPassword,boardWriteUserMapping.getPassword());
        }
        return false;
    }

    //게시글의 유효성 체크
    public String isBoardWriteValid(BoardDto boardDto, Authentication authentication,String mode)
    {
        try
        {
            String title = boardDto.getTitle();
            String content = boardDto.getContent();
            String author = boardDto.getUser();
            String password = boardDto.getPassword();
            Boolean isUser = this.roleService.isUser(authentication);
            Boolean isWriteMode = mode.equals("write");
            String result = "통과";

            if(utilService.isLRWhiteSpace(author))
            {
                return "닉네임의 앞 뒤에는 공백이 올 수 없습니다";
            }

            if(utilService.isThereWhiteSpace(password))
            {
                return "비밀번호에는 공백이 올 수 없습니다";
            }

            if(title.length() < 2 || title.length() > 20)
            {
                result = "제목이 2자 미만이거나 20자 초과합니다";
            }

            if(content.length() < 2 || content.getBytes().length > 65535)
            {
                result = "내용이 2자 미만이거나 65535byte를 초과합니다";
                if(content.getBytes().length > 65535)
                {
                    result += "\n현재 byte : " + content.getBytes().length;
                }
            }

            if(!isUser && isWriteMode)
            {
                if(author.length() < 2 || author.length() > 20)
                {
                    result = "글쓴이가 2자 미만이거나 20자 초과합니다";
                }
            }

            if(!isUser && isWriteMode)
            {
                if(password.length() < 4 || password.length() > 20)
                {
                    result = "비밀번호가 4자 미만이거나 20자 초과합니다";
                }
            }
            return result;
        }
        catch (Exception e)
        {
            return "예기치 못한 에러입니다";
        }
    }

    public Boolean isThereBoardInGallery(String gallery,Long boardIdx)
    {
        if(boardRepository.findByGalleryAndContentIdx(gallery,boardIdx,Board.class) == null)
        {
            return false;
        }
        return true;
    }

    //전체 갤러리에서 인기글 (메인 컨트롤러에서 사용)
    public List<BoardDto> getMostLikelyBoardList()
    {
        List<Board> boardList = boardRepository.findTop10ByOrderByLikesLikeDesc();
        List<BoardDto> boardDtoList = new ArrayList<BoardDto>();
        for(Board board : boardList)
        {
            if(board.getLikes().getLike() == 0L)
            {
                break;
            }
            if(board.getTitle().length() > 20)
            {
                board.updateTitle(board.getTitle().substring(19) + "...");
            }
            boardDtoList.add(this.convertEntityToDto(board));
        }
        return boardDtoList;
    }



    //해당 갤러리에서의 개념글
    public List<BoardDto> getMostLikelyBoardListByFindObj(String findObj,Integer page,String mode)
    {
        List<BoardDto> boardDtoList = new ArrayList<>();
        //갤러리에 개념글 기준 개추 수록
        //개추는 최소 10 이상
        //댓글은 개추 / 3 이어야함
        //댓글이 총 몇개인지 가져와야함

        Page<Board> pageEntity;
        if(mode.equals("mypage"))
        {
            pageEntity = this.boardRepository.findByUserAndBoardInfoIsRecommendedBoardIsTrue(findObj,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }
        else
        {
            pageEntity = this.boardRepository.findByGalleryAndBoardInfoIsRecommendedBoardIsTrue(findObj,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }


        List<Board> boardList = pageEntity.getContent();

        for(Board board : boardList)
        {
            boardDtoList.add(this.convertEntityToDto(board));
        }

        return boardDtoList;
    }

    public void getBoardAndUpdateGalleryRecommendInfo(String galleryName, Long boardIdx,Board board)
    {
        if(board.getBoardInfo().getIsRecommendedBoard())
        {
            ////추천누르면 -> 일단 이미 개념글인 애이면 gallery에서 max likes += 1
            galleryService.updateRecommendInfo(galleryName,1L,false);
        }
    }

    public void updateIsRecommendBoard(String galleryName, Long boardIdx,Board board)
    {
        Long recommendStandard;
        Long boardRecommend;
        Long countComment;

        recommendStandard = this.galleryService.getRecommendStandard(galleryName);
        //갤러리 개추기준

        boardRecommend = board.getLikes().getLike();
        //보드 개추
        countComment = this.commentService.countByBoardIdx(boardIdx);
        //댓글 몇개인지 체크하여 개추여부 수정

        if(boardRecommend >= recommendStandard && countComment >= recommendStandard / 3)
        {
            boardRepository.save(board);
            //처음 개념글 되는애면 gallery에서 count랑 like 갱신해줘야함
            galleryService.updateRecommendInfo(galleryName,boardRecommend,!(board.getBoardInfo().getIsRecommendedBoard()));
            board.getBoardInfo().updateIsRecommendedBoard();
        }
    }


    public Long getCountBoardByGallery(String galleryName)
    {
        return this.boardRepository.countBoardByGallery(galleryName);
    }

    //유저가 작성한 글 가져오기
    public void addingPagedBoardToModelByWritedUser(Model model,int page,String pagingMode,String username)
    {
        List<BoardDto> boardDtoList;
        Long count;
        Integer[] pageList;

        //유저가 작성한 글 중 개념글인 글들만 가져오기
        if(pagingMode.equals("mostLikedBoard"))
        {
            boardDtoList = getMostLikelyBoardListByFindObj(username,page,"mypage");
            count = getCountRecommendedBoardByUsername(username);
            pageList = getPageList(page,count);
        }
        //유저가 작성한 전체 글
        else
        {
            boardDtoList = getPostByFindObj(username,page,"mypage");
            count = getCountBoardByUsername(username);
            pageList = getPageList(page,count);
        }

        model.addAttribute("postList",boardDtoList);
        model.addAttribute("pageList",pageList);
    }

    private Long getCountBoardByUsername(String username)
    {
        return this.boardRepository.countBoardByUserAndIsLoggedUser(username,1L);
    }

    private Long getCountRecommendedBoardByUsername(String username)
    {
        return this.boardRepository.countBoardByUserAndBoardInfoIsRecommendedBoardIsTrueAndIsLoggedUser(username,1L);
    }




    public void addingPagedBoardToModel(Model model, String galleryName, int page, String pagingMode)
    {
        List<BoardDto> boardDtoList;
        Long count;
        Integer[] pageList;

        if(pagingMode.equals("mostLikedBoard"))
        {
            boardDtoList = getMostLikelyBoardListByFindObj(galleryName,page, "board");
            count = galleryService.getCountRecommendedBoardByGalleryName(galleryName);
            pageList = getPageList(page,count);
        }
        else
        {
            boardDtoList = getPostByFindObj(galleryName,page,"board");
            count = getCountBoardByGallery(galleryName);
            pageList = getPageList(page,count);
        }
        model.addAttribute("pageList",pageList);
        model.addAttribute("postList",boardDtoList);
    }

    public void addVisitedNum(Long boardIdx)
    {
        Board board = this.boardRepository.getById(boardIdx);
        board.getBoardInfo().updateVisitedNum();
        this.boardRepository.save(board);
    }




    //해당 갤러리에서의 개념글
    public List<BoardDto> getBoardListBySearchKeyword(String keyword,String searchMode,Integer page,Boolean isRecommendedBoardMode)
    {
        List<BoardDto> boardDtoList = new ArrayList<>();
        Page<Board> pageEntity;
        if(searchMode.equals("title"))
        {
            pageEntity = this.boardRepository.findByTitleContainingAndBoardInfoIsRecommendedBoard(keyword,isRecommendedBoardMode,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }
        else
        {
            pageEntity = this.boardRepository.findByContentContainingAndBoardInfoIsRecommendedBoard(keyword,isRecommendedBoardMode,
                    PageRequest.of(page - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
        }

        List<Board> boardList = pageEntity.getContent();

        for(Board board : boardList)
        {
            boardDtoList.add(this.convertEntityToDto(board));
        }

        return boardDtoList;
    }

    public Long getCountBoardByKeyword(String keyword,String searchMode)
    {
        if(searchMode.equals("title"))
        {
            return this.boardRepository.countBoardByTitleContaining(keyword);
        }
        return this.boardRepository.countBoardByContentContaining(keyword);
    }


    public void addingPagedBoardToModelByKeyword(Model model, String keyword, String searchMode,Integer page, String pagingMode)
    {
        List<BoardDto> boardDtoList;
        Long count;
        Integer[] pageList;
        Boolean isRecommendedBoardMode = pagingMode.equals("mostLikedBoard");

        boardDtoList = getBoardListBySearchKeyword(keyword,searchMode,page, isRecommendedBoardMode);
        count = getCountBoardByKeyword(keyword,searchMode);
        pageList = getPageList(page,count);

        model.addAttribute("pageList",pageList);
        model.addAttribute("postList",boardDtoList);
    }
}


