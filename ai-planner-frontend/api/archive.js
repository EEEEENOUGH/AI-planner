import request from '@/utils/request'

/**
 * 查询当前用户的启用备考档案
 * @returns {Promise<ArchiveVO>}
 */
export function getMyArchive() {
  return request({
    url: '/archive/me',
    method: 'GET'
  })
}

/**
 * 新建或更新备考档案（后端按用户 id 做 upsert）
 * @param {{
 *   archiveName?: string,
 *   targetInstitution: string,
 *   targetMajor: string,
 *   examDate: string,
 *   examSubjects: string,
 *   dailyStudyDuration: number,
 *   subjectMastery?: string
 * }} data
 */
export function saveArchive(data) {
  return request({
    url: '/archive/save',
    method: 'POST',
    data
  })
}
