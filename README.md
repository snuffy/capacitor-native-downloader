# capacitor-native-downloader

native file downloader

## Install

```bash
npm install capacitor-native-downloader
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`add(...)`](#add)
* [`start(...)`](#start)
* [`pause(...)`](#pause)
* [`resume(...)`](#resume)
* [`cancel(...)`](#cancel)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### add(...)

```typescript
add(options: { params: IAddDownloadRequest; }) => Promise<IAddDownloadResponse>
```

| Param         | Type                                                                             |
| ------------- | -------------------------------------------------------------------------------- |
| **`options`** | <code>{ params: <a href="#iadddownloadrequest">IAddDownloadRequest</a>; }</code> |

**Returns:** <code>Promise&lt;<a href="#iadddownloadresponse">IAddDownloadResponse</a>&gt;</code>

--------------------


### start(...)

```typescript
start(options: { params: IStartDownloadRequest; }) => Promise<IStartDownloadResponse>
```

| Param         | Type                                                                                 |
| ------------- | ------------------------------------------------------------------------------------ |
| **`options`** | <code>{ params: <a href="#istartdownloadrequest">IStartDownloadRequest</a>; }</code> |

**Returns:** <code>Promise&lt;<a href="#istartdownloadresponse">IStartDownloadResponse</a>&gt;</code>

--------------------


### pause(...)

```typescript
pause(options: { params: IPauseDownloadRequest; }) => Promise<IPauseDownloadResponse>
```

| Param         | Type                                                                                 |
| ------------- | ------------------------------------------------------------------------------------ |
| **`options`** | <code>{ params: <a href="#ipausedownloadrequest">IPauseDownloadRequest</a>; }</code> |

**Returns:** <code>Promise&lt;<a href="#ipausedownloadresponse">IPauseDownloadResponse</a>&gt;</code>

--------------------


### resume(...)

```typescript
resume(options: { params: IResumeDownloadRequest; }) => Promise<IResumeDownloadResponse>
```

| Param         | Type                                                                                   |
| ------------- | -------------------------------------------------------------------------------------- |
| **`options`** | <code>{ params: <a href="#iresumedownloadrequest">IResumeDownloadRequest</a>; }</code> |

**Returns:** <code>Promise&lt;<a href="#iresumedownloadresponse">IResumeDownloadResponse</a>&gt;</code>

--------------------


### cancel(...)

```typescript
cancel(options: { params: ICancelDownloadRequest; }) => Promise<ICancelDownloadResponse>
```

| Param         | Type                                                                                   |
| ------------- | -------------------------------------------------------------------------------------- |
| **`options`** | <code>{ params: <a href="#icanceldownloadrequest">ICancelDownloadRequest</a>; }</code> |

**Returns:** <code>Promise&lt;<a href="#icanceldownloadresponse">ICancelDownloadResponse</a>&gt;</code>

--------------------


### Interfaces


#### IAddDownloadResponse

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`fileName`**     | <code>string</code> |
| **`absolutePath`** | <code>string</code> |


#### IAddDownloadRequest

| Prop              | Type                                    |
| ----------------- | --------------------------------------- |
| **`id`**          | <code>string</code>                     |
| **`url`**         | <code>string</code>                     |
| **`size`**        | <code>number</code>                     |
| **`filePath`**    | <code>string</code>                     |
| **`fileName`**    | <code>string</code>                     |
| **`displayName`** | <code>string</code>                     |
| **`headers`**     | <code>{ authorization: string; }</code> |


#### IStartDownloadResponse

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`filename`**     | <code>string</code> |
| **`absolutePath`** | <code>string</code> |


#### IStartDownloadRequest

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### IPauseDownloadResponse

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`filename`**     | <code>string</code> |
| **`absolutePath`** | <code>string</code> |


#### IPauseDownloadRequest

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### IResumeDownloadResponse

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`filename`**     | <code>string</code> |
| **`absolutePath`** | <code>string</code> |


#### IResumeDownloadRequest

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### ICancelDownloadResponse

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`filename`**     | <code>string</code> |
| **`absolutePath`** | <code>string</code> |


#### ICancelDownloadRequest

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |

</docgen-api>
