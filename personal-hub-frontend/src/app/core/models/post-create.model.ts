export interface PostCreateModel {
  title: string;
  content: string;
  categoryID: number;
  tagIDs?: number[];
  images?: File[];
}